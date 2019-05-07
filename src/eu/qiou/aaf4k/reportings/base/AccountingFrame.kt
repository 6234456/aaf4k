package eu.qiou.aaf4k.reportings.base

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.groupNearby
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import kotlin.streams.toList

class AccountingFrame(override val id: Long, override val name: String, override val desc: String = "", val structure: List<ProtoAccount>): ProtoCollectionAccount {
    override val subAccounts: MutableList<ProtoAccount> = mutableListOf()
    override val decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION
    override val unit: CurrencyUnit = CurrencyUnit()
    override val superAccounts: MutableList<ProtoCollectionAccount> = mutableListOf()
    override val timeParameters: TimeParameters? = null
    override val entity: Entity? = null
    override val isStatistical: Boolean = false
    override val validateUntil: LocalDate? = null
    override val displayUnit: ProtoUnit = CurrencyUnit()

    override fun deepCopy(): ProtoAccount {
        throw Exception("AccountingFrame can not be copied.")
    }

    override var toUpdate = false

    override var cacheList: List<ProtoAccount> = listOf()
    override var cacheAllList: List<ProtoAccount> = listOf()


    fun toReporting(id: Long = this.id, name: String = this.name,
                    timeParameters: TimeParameters = GlobalConfiguration.DEFAULT_TIME_PARAMETERS,
                    desc: String = "",
                    displayUnit: CurrencyUnit = CurrencyUnit(),
                    entity: Entity = GlobalConfiguration.DEFAULT_REPORTING_ENTITY): Reporting {
        return Reporting(CollectionAccount(id, name, timeParameters = timeParameters,
                desc = desc, displayUnit = displayUnit, entity = entity).apply {
                    addAll(structure)
                })
    }

    companion object {

        fun inflate(id: Long, frame: String, fileName: String): AccountingFrame {

            val lines = Files.lines(Paths.get(fileName)).toList().filter { !it.isBlank() }

            val regIndent = """^(\s*)(\[?)(\d+)""".toRegex()
            val regType = """^\s*[A-Z]{2}\s*$""".toRegex()


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
                            .filter { x-> pairs[x].first == targLevel }
                }

                val notHasChild: (Int) -> Boolean = {
                    it == size - 1 || pairs[it + 1].first <= pairs[it].first
                }

                val type: (String) -> ReportingType = Account.parseReportingType

                val types: (List<String>) -> ReportingType? = {
                    if (it.count() > 2 && regType.containsMatchIn(it.last()))
                        type(it.last())
                    else
                        null
                }

                var lastType: ReportingType = ReportingType.ASSET
                var tmpType: ReportingType?

                fun getParentTypeRecursively(i: Int): ReportingType? {
                    val p = getParent(i)
                    val e = types(this[p].last().split("#"))

                    if (e == null && pairs[p].first == 0)
                        return null

                    return e ?: getParentTypeRecursively(p)
                }

                // the type-attribute will be inherited directly from the parent recursively
                // if the uttermost level reached without type specified, adopt the very first type

                val parentTypes = this.mapIndexed { i, e ->
                    if (i == 0) {
                        lastType = types(e.last().split("#")) ?: ReportingType.ASSET // default for Translator
                        lastType
                    } else {
                        tmpType = getParentTypeRecursively(i)
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

                fun parse(s: String, t: ReportingType? = null): ProtoAccount {
                    val arr = s.split("#")
                    val name = arr[1]
                    regIndent.find(s)?.groups!!.let {
                        return Account(it[3]!!.value.toLong(),
                                name, decimalPrecision = 2, value = 0,
                                isStatistical = it[2]!!.value.length == 1,
                                reportingType = if (types(arr) == null) t!!
                                else types(arr)!!
                        )
                    }
                }

                fun parseSuperAccount(src: String, acc: List<ProtoAccount>, t: ReportingType? = null): ProtoAccount {
                    val arr = src.split("#")
                    val name = arr[1]
                    regIndent.find(src)?.groups!!.let {
                        return CollectionAccount(it[3]!!.value.toLong(), name, decimalPrecision = 2,
                                isStatistical = it[2]!!.value.length == 1,
                                reportingType = if (types(arr) == null) t!! else types(arr)!!
                        ).apply {
                            addAll(acc)
                        }
                    }
                }

                fun scopeToAccount(i: Int): List<ProtoAccount> {
                    return when (scope(i) - i) {
                        1 -> this[i].map { parse(it, getParentType(i)) }
                        else -> directChildren(i).fold(listOf()) { acc, index ->
                            if (!notHasChild(index))
                                acc + this[index].dropLast(1).map { parse(it, getParentType(index)) } +
                                        parseSuperAccount(this[index].last(), scopeToAccount(index + 1), getParentType(index + 1))
                            else
                                acc + this[index].map { parse(it, getParentType(index)) }
                        }
                    }
                }

                return AccountingFrame(id, frame, structure = scopeToAccount(0))
            }
        }

        /**
         * @param fileName  the name of frame file in format "cn_cas_2018"
         */
        fun inflate(id: Long, fileName: String): AccountingFrame {
            val f = if (fileName.endsWith(".txt")) fileName.removeSuffix(".txt") else fileName
            val (dir, frame, _) = f.split("_")

            return inflate(id, frame, "data/$dir/$f.txt")
        }
    }
}