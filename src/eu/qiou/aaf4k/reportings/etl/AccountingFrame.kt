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

class AccountingFrame(id: Int, name: String, accounts: List<Account>) : Reporting(id, name, timeParameters = TimeParameters(), structure = accounts) {
    companion object {
        /**
         * @param fileName  the name of frame file in format "cn_cas_2018"
         */
        fun inflate(id: Int, fileName: String): AccountingFrame {
            val f = if (fileName.endsWith(".txt")) fileName.removeSuffix(".txt") else fileName
            val (dir, frame, _) = f.split("_")

            val regIndent = """^(\s*)(\[?)(\d+)""".toRegex()
            val lines = Files.lines(Paths.get("data/${dir}/${f}.txt")).toList().filter { !it.isBlank() }

            with(lines.filter { !regIndent.containsMatchIn(it) }) {
                if (this.count() > 0) {
                    throw Exception("AccountingFrameStructureError: $this ")
                }
            }

            val toLevel: (String) -> Int = {
                regIndent.find(it)?.groups?.get(1)!!.value.length / 4
            }

            val parse: (String) -> Account = {
                val arr = it.split("#")
                val name = arr[1]
                regIndent.find(it)?.groups!!.let {
                    val stat = it.get(2)!!.value.length == 1
                    val id = it.get(3)!!.value.toInt()

                    Account(id, name, decimalPrecision = 2, value = 0, isStatistical = stat, reportingType = ReportingType.LIABILITY)
                }
            }

            val parseSuperAccount: (String, List<Account>) -> Account = { src, acc ->

                val arr = src.split("#")
                val name = arr[1]
                regIndent.find(src)?.groups!!.let {
                    val stat = it.get(2)!!.value.length == 1
                    val id = it.get(3)!!.value.toInt()

                    Account(id, name, decimalPrecision = 2, subAccounts = acc.toMutableSet(), isStatistical = stat, reportingType = ReportingType.LIABILITY)
                }
            }




            with(lines.groupNearby(toLevel)) {
                val size = this.size
                // level to String
                val pairs = this.map { toLevel(it[0]) }.zip(this)

                val scope: (Int) -> Int = {
                    val targLevel = pairs[it].first
                    var res = it + 1

                    //loop through, if lvl <= targLevel break, till size -1
                    while (res < size) {
                        if (targLevel >= pairs[res].first)
                            break

                        res++
                    }
                    res
                }

                val getParent: (Int) -> Int = {
                    var res = it

                    while (res > 0) {
                        if (pairs[res].first == 0) {
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


                fun scopeToAccount(i: Int): List<Account> {
                    return when (scope(i) - i) {
                        1 -> this[i].map { parse(it) }
                        else -> directChildren(i).fold(listOf<Account>()) { acc, index ->
                            if (!notHasChild(index))
                                acc + this[index].dropLast(1).map(parse) +
                                        parseSuperAccount(this[index].last(), scopeToAccount(index + 1))
                            else
                                acc + this[index].map(parse)
                        }
                        /*
                        directChildren(i).fold(this[i].dropLast(1).map(parse)){
                                         acc, index ->
                                            if (notHasChild(index))
                                                acc + this[index].map(parse)
                                            else
                                            acc + parseSuperAccount(this[index].last() , scopeToAccount(index))
                                    }
                                    */
                    }
                }

                return AccountingFrame(id, frame, scopeToAccount(0))
            }
        }
    }
}