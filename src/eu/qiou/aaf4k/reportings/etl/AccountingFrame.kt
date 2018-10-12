package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.accounting.model.Account
import eu.qiou.aaf4k.accounting.model.Reporting
import eu.qiou.aaf4k.accounting.model.ReportingType
import eu.qiou.aaf4k.util.groupNearby
import eu.qiou.aaf4k.util.time.TimeParameters
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList

/**
 * in some cases, the account data info is unknown, but the structure is pre-defined based on, for example, the account id.
 * As to the SKR3 of the German accounting, the accounts with an id ranging from 8400 to 8900 belongs to category the revenue.
 * When the definition of the new accounts follows the given pattern, i.e. regulation of the accounting frame, there exists no need to pre-define the whole structure in advance.
 *
 * the orginially loosely coupled data and structure are combined in the accounting frame
 */

class AccountingFrame(id: Int, name: String, val accounts: List<Account>) :
        Reporting(id, name, structure = accounts) {

    // TODO: complete the adaptor
    fun toReporting(id: Int, name: String, timeParameters: TimeParameters = TimeParameters(), desc: String = ""): Reporting {
        return Reporting(id, name, timeParameters = timeParameters, structure = accounts, desc = desc)
    }

    companion object {
        /**
         * @param fileName  the name of frame file in format "cn_cas_2018"
         */
        fun inflate(id: Int, fileName: String): AccountingFrame {
            val f = if (fileName.endsWith(".txt")) fileName.removeSuffix(".txt") else fileName
            val (dir, frame, _) = f.split("_")

            val regIndent = """^(\s*)(\[?)(\d+)""".toRegex()
            val regType = """^\s*[A-Z]{2}\s*$""".toRegex()
            val lines = Files.lines(Paths.get("data/${dir}/${f}.txt")).toList().filter { !it.isBlank() }


            // throw error in case of illegal indent
            with(lines.filter { !regIndent.containsMatchIn(it) }) {
                if (this.count() > 0) {
                    throw Exception("AccountingFrameStructureError: $this ")
                }
            }

            // return the indent level based on the affix blanks
            // by default indent with tab = 4 * blank
            val toLevel: (String) -> Int = {
                regIndent.find(it)?.groups?.get(1)!!.value.length / 4
            }

            // group the adjacent lines with the same indent level
            with(lines.groupNearby(toLevel)) {
                val size = this.size
                // level to String, the first element is the level the second string
                val pairs = this.map { toLevel(it[0]) }.zip(this)

                // with the index of the parent account, get the scope length of all its children direct and indirect
                val scope: (Int) -> Int = {
                    val targLevel = pairs[it].first
                    var res = it + 1

                    //loop through, if lvl <= targLevel break, till size -1, to the end of the list or to its next sibling or parent
                    while (res < size) {
                        if (targLevel >= pairs[res].first)
                            break
                        res++
                    }
                    res
                }

                // iterate up to the immediate parent, with the current index
                val getParent: (Int) -> Int = {
                    var res = it
                    var lvl = pairs[it].first - 1
                    lvl = if (lvl < 0) 0 else lvl

                    while (res > 0) {
                        if (pairs[res].first == lvl) {
                            break
                        }
                        res--
                    }
                    res
                }
                // index of elements of the same level
                val directChildren: (Int) -> List<Int> = {
                    val targLevel = pairs[it].first
                    (if (it == 0)
                        it.until(size)
                    else
                        it.until(scope(getParent(it))))
                            .filter { pairs[it].first == targLevel }
                }

                val notHasChild: (Int) -> Boolean = {
                    it == size - 1 || pairs[it + 1].first <= pairs[it].first
                }

                val type: (String) -> ReportingType = {
                    when (it) {
                        ReportingType.ASSET.code -> ReportingType.ASSET
                        ReportingType.EQUITY.code -> ReportingType.EQUITY
                        ReportingType.LIABILITY.code -> ReportingType.LIABILITY
                        ReportingType.REVENUE_GAIN.code -> ReportingType.REVENUE_GAIN
                        ReportingType.EXPENSE_LOSS.code -> ReportingType.EXPENSE_LOSS
                        ReportingType.PROFIT_LOSS_NEUTRAL.code -> ReportingType.PROFIT_LOSS_NEUTRAL
                        ReportingType.ANNUAL_RESULT.code -> ReportingType.ANNUAL_RESULT
                        ReportingType.AUTO.code -> ReportingType.AUTO
                        else -> throw java.lang.Exception("ParameterError: unknown ReportingType:$it")
                    }
                }

                val types: (List<String>) -> ReportingType? = {
                    if (it.count() > 2 && regType.containsMatchIn(it.last()))
                        type(it.last())
                    else
                        null
                }

                var lastType: ReportingType = ReportingType.ASSET
                var tmpType: ReportingType?

                val parentTypes = this.mapIndexed { i, e ->
                    if (i == 0) {
                        lastType = types(e.last().split("#"))!!
                        lastType
                    } else {
                        tmpType = types(this[i - 1].last().split("#"))
                        if (tmpType == null) {
                            lastType
                        } else {
                            tmpType!!
                        }
                    }
                }

                fun getParentType(index: Int): ReportingType {
                    return parentTypes[index]
                }

                fun parse(s: String, t: ReportingType? = null): Account {
                    val arr = s.split("#")
                    val name = arr[1]
                    regIndent.find(s)?.groups!!.let {
                        return Account(it.get(3)!!.value.toInt(),
                                name, decimalPrecision = 2, value = 0,
                                isStatistical = it.get(2)!!.value.length == 1,
                                reportingType = if (types(arr) == null) t!!
                                else types(arr)!!
                        )
                    }
                }

                fun parseSuperAccount(src: String, acc: List<Account>, t: ReportingType? = null): Account {
                    val arr = src.split("#")
                    val name = arr[1]
                    regIndent.find(src)?.groups!!.let {
                        return Account(it.get(3)!!.value.toInt(), name, decimalPrecision = 2,
                                subAccounts = acc.toMutableSet(),
                                isStatistical = it.get(2)!!.value.length == 1,
                                reportingType = if (types(arr) == null) t!! else types(arr)!!
                        )
                    }
                }

                fun scopeToAccount(i: Int): List<Account> {
                    return when (scope(i) - i) {
                        1 -> this[i].map { parse(it, getParentType(i)) }
                        else -> directChildren(i).fold(listOf<Account>()) { acc, index ->
                            if (!notHasChild(index))
                                acc + this[index].dropLast(1).map { parse(it, getParentType(index)) } +
                                        parseSuperAccount(this[index].last(), scopeToAccount(index + 1), getParentType(index + 1))
                            else
                                acc + this[index].map { parse(it, getParentType(index)) }
                        }
                    }
                }

                return AccountingFrame(id, frame, scopeToAccount(0))
            }
        }
    }
}