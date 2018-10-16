package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.foldTrackListInit
import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mergeReduce
import eu.qiou.aaf4k.util.mkString
import eu.qiou.aaf4k.util.strings.CollectionToString
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellUtil


/**
 * Class of ProtoReporting
 *
 * the object model of the reporting
 *
 * ProtoReporting - ProtoCategory - ProtoEntry - (ProtoAccount)
 *
 * ProtoAccount is not always attached to a ProtoEntry
 *
 * Reporting can be deemed as a list of <b>structured</b> ProtoAccounts
 * while Category is a set of Entries, which is not structured
 *
 * initial value of ProtoReporting is null, each state of the Reporting is immutable. The Reporting is updated by means of Category
 * ProtoReporting = ProtoReporting + ProtoCategory
 *
 * ProtoCategory is a collection of ProtoEntries, which represent the delta of ProtoReporting.
 *
 * @property structure list of accounts in structure
 */
open class ProtoReporting<T : ProtoAccount>(val id: Int, val name: String, val desc: String = "", val structure: List<T>,
                                            val displayUnit: ProtoUnit = CurrencyUnit(), val entity: ProtoEntity = GlobalConfiguration.DEFAULT_ENTITY,
                                            val timeParameters: TimeParameters = GlobalConfiguration.DEFAULT_TIME_PARAMETERS) : JSONable {

    val categories: MutableSet<ProtoCategory<T>> = mutableSetOf()
    val flattened: List<T> = this.flatten()

    fun mergeCategories(): Map<Int, Double> {
        return categories.map { it.toDataMap() }.reduce { acc, map ->
            acc.mergeReduce(map) { a, b -> a + b }
        }
    }

    fun shorten(): ProtoReporting<T> {
        return ProtoReporting(id, name, desc,
                structure.map { it.shorten() as T }
                , displayUnit, entity, timeParameters)
    }

    /**
     * return one dimensional array of atomic accounts
     */
    fun flatten(): List<T> {
        return structure.filter { !it.isStatistical }.map { if (it.hasChildren()) it.flatten() else mutableListOf(it as Drilldownable) }.reduce { acc, mutableList ->
            acc.apply { addAll(mutableList) }
        } as List<T>
    }

    /**
     * after update through the categories
     * get the reporting
     */
    fun generate(): ProtoReporting<T> {
        return update(mergeCategories())
    }

    open fun findAccountByID(id: Int): T? {
        structure.forEach {
            it.findChildByID(id)?.let {
                return it as T
            }
        }

        return null
    }

    open fun update(entry: ProtoEntry<T>, updateMethod: (Double, Double) -> Double = { valueNew, valueOld -> valueNew + valueOld }): ProtoReporting<T> {
        return ProtoReporting(id, name, desc,
                structure.map { it.deepCopy(entry, updateMethod) }
                , displayUnit, entity, timeParameters)
    }

    open fun update(category: ProtoCategory<T>, updateMethod: (Double, Double) -> Double = { valueNew, valueOld -> valueNew + valueOld }): ProtoReporting<T> {
        return ProtoReporting<T>(id, name, desc,
                structure.map { it.deepCopy(category, updateMethod) }
                , displayUnit, entity, timeParameters)
    }

    open fun update(data: Map<Int, Double>, updateMethod: (Double, Double) -> Double = { valueNew, valueOld -> valueNew + valueOld }): ProtoReporting<T> {
        return ProtoReporting<T>(id, name, desc,
                structure.map { it.deepCopy<T>(data, updateMethod) }
                , displayUnit, entity, timeParameters)
    }

    override fun toJSON():String{
        return CollectionToString.mkJSON(structure)
    }

    override fun toString(): String {
        return CollectionToString.mkString(structure)
    }

    fun toXl(path: String) {
        val startRow = 1
        var cnt = startRow
        val col_id = 0
        val col_name = col_id + 1
        val col_original = col_name + 1
        var col = col_original + 1
        fun writeAccountToXl(account: ProtoAccount, sht: Sheet, indent: Int = 0) {

            val l = account.countRecursively(true)
            val lvl = account.levels()

            sht.createRow(cnt++).apply {
                createCell(col_id).setCellValue(account.id.toDouble())
                createCell(col_name).setCellValue("${if (account.isStatistical) "其中:" else ""}${account.name}")

                if (l > 1) {
                    if (lvl == 2)
                        createCell(col_original).cellFormula =
                                "SUM(${CellUtil.getCell(CellUtil.getRow(this.rowNum + 1, sht), col_original).address}:" +
                                "${CellUtil.getCell(CellUtil.getRow(this.rowNum + l - 1, sht), col_original).address}" +
                                ")"
                    else
                        createCell(col_original).cellFormula = account.subAccounts!!.foldTrackListInit(0) { a, protoAccount, _ ->
                            a + protoAccount.countRecursively(true)
                        }.dropLast(1).map {
                            CellUtil.getCell(CellUtil.getRow(this.rowNum + 1 + it, sht), col_original).address
                        }
                                .mkString("+", prefix = "", affix = "")
                } else
                    createCell(col_original).setCellValue(account.displayValue)

                ExcelUtil.Update(getCell(col_original)).numberFormat(account.decimalPrecision)
            }
            account.subAccounts?.let {
                it.forEach {
                    writeAccountToXl(it, sht, indent + 1)
                }

                if (l > 2 && account.levels() == 2) {
                    sht.groupRow(cnt - l + 1, cnt - 1)
                }
            }
        }

        var shtOverview: Sheet? = null

        ExcelUtil.createWorksheetIfNotExists(path, callback = { sht ->
            this.structure.forEach {
                writeAccountToXl(it, sht)
            }

            shtOverview = sht
        })

        cnt = startRow

        var data = mutableMapOf<Int, String>()

        ExcelUtil.createWorksheetIfNotExists(path, "adj", { shtCat ->
            this.categories.forEach {
                it.entries.forEach {
                    it.accounts.forEach { acc ->
                        shtCat.createRow(cnt++).apply {
                            this.createCell(0).setCellValue(acc.id.toDouble())
                            this.createCell(1).setCellValue(acc.name)
                            this.createCell(2).setCellValue(acc.displayValue)
                            this.createCell(3).setCellValue(it.desc)

                            if (data.containsKey(acc.id)) {
                                data[acc.id] = "${data[acc.id]}+'${shtCat.sheetName}'!${CellUtil.getCell(this, 2).address}"
                            } else {
                                data[acc.id] = "'${shtCat.sheetName}'!${CellUtil.getCell(this, 2).address}"
                            }
                        }
                    }

                    shtCat.createRow(cnt++)
                }
            }
        })

        ExcelUtil.unload(data, { it.toDouble().toInt() }, 0, col, shtOverview!!, { false }, { c, v ->
            c.cellFormula = v
            ExcelUtil.Update(c).numberFormat(GlobalConfiguration.DEFAULT_DECIMAL_PRECISION)
        }, path)

    }
}