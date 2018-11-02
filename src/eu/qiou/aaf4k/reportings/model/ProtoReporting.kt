package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.foldTrackListInit
import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mergeReduce
import eu.qiou.aaf4k.util.mkJSON
import eu.qiou.aaf4k.util.mkString
import eu.qiou.aaf4k.util.strings.CollectionToString
import eu.qiou.aaf4k.util.template.Template
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CellType
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

    fun addCategory(category: ProtoCategory<T>) {
        if (categories.any { it.id == category.id })
            throw Exception("Duplicated Category-ID ${category.id} in $categories")

        categories.add(category)
    }

    fun mergeCategories(): Map<Int, Double> {
        return if (categories.isEmpty()) mapOf() else categories.map { it.toDataMap() }.reduce { acc, map ->
            acc.mergeReduce(map) { a, b -> a + b }
        }
    }

    fun shorten(): ProtoReporting<T> {
        return ProtoReporting(id, name, desc,
                structure.map { it.shorten() as T }
                , displayUnit, entity, timeParameters)
    }

    fun nullify(): ProtoReporting<T> {
        return ProtoReporting(id, name, desc,
                structure.map { it.nullify() as T }
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

    fun flattenWithStatistical(): List<T> {
        return structure.map { if (it.hasChildren()) it.flatten() else mutableListOf(it as Drilldownable) }.reduce { acc, mutableList ->
            acc.apply { addAll(mutableList) }
        } as List<T>
    }

    fun flattenWithAllAccounts(): List<T> {
        return structure.fold(listOf()) { acc, t ->
            (acc + t.flattenIncludeSelf()) as List<T>
        }
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
        return """{"id":$id, "name":"$name", "desc":"$desc", "structure":${CollectionToString.mkJSON(structure)}, "entity":${entity.toJSON()}, "timeParameters":${timeParameters.toJSON()}, "categories":${categories.mkJSON()}}"""
    }

    override fun toString(): String {
        return CollectionToString.mkString(structure)
    }

    fun toXl(path: String, titleID: String = "科目代码", titleName: String = "科目名称"
             , titleOriginal: String = "账户余额:调整前", titleFinal: String = "账户余额:调整后", prefixStatistical: String = " 其中: "
    ) {
        val startRow = 1
        var cnt = startRow
        val colId = 0
        val colName = colId + 1
        val colOriginal = colName + 1
        val colLast = colOriginal + categories.size + 1
        var col = colOriginal + 1
        var heading: CellStyle? = null
        var light: CellStyle? = null
        var dark: CellStyle? = null
        var shtOverview: Sheet? = null


        fun writeAccountToXl(account: ProtoAccount, sht: Sheet, indent: Int = 0) {

            val l = account.countRecursively(true)
            val lvl = account.levels()

            col = colOriginal + 1

            sht.createRow(cnt++).apply {
                createCell(colId).setCellValue(account.id.toDouble())
                createCell(colName).setCellValue("${if (account.isStatistical) prefixStatistical else ""}${account.name}")

                if (l > 1) {
                    if (lvl == 2) {
                        createCell(colOriginal).cellFormula =
                                "SUM(${CellUtil.getCell(CellUtil.getRow(this.rowNum + 1, sht), colOriginal).address}:" +
                                "${CellUtil.getCell(CellUtil.getRow(this.rowNum + l - 1, sht), colOriginal).address}" +
                                ")"

                        this@ProtoReporting.categories.forEach {
                            createCell(col).cellFormula = "SUM(${CellUtil.getCell(CellUtil.getRow(this.rowNum + 1, sht), col).address}:" +
                                    "${CellUtil.getCell(CellUtil.getRow(this.rowNum + l - 1, sht), col).address}" +
                                    ")"
                            col++
                        }
                        col = colOriginal + 1
                    } else {
                        val tmp = account.subAccounts!!.foldTrackListInit(0) { a, protoAccount, _ ->
                            a + protoAccount.countRecursively(true)
                        }.dropLast(1)

                        createCell(colOriginal).cellFormula = tmp.map {
                            CellUtil.getCell(CellUtil.getRow(this.rowNum + 1 + it, sht), colOriginal).address
                        }.mkString("+", prefix = "", affix = "")


                        this@ProtoReporting.categories.forEach {
                            createCell(col).cellFormula = tmp.map {
                                CellUtil.getCell(CellUtil.getRow(this.rowNum + 1 + it, sht), col).address
                            }.mkString("+", prefix = "", affix = "")
                            col++
                        }
                        col = colOriginal + 1

                    }
                } else
                    createCell(colOriginal).setCellValue(account.displayValue)

                colId.until(colLast + 1).forEach { i ->
                    val c = getCell(i) ?: createCell(i, CellType.NUMERIC)

                    ExcelUtil.Update(c).style(if (rowNum % 2 == 0) light!! else dark!!).indent(if (c.columnIndex == colName) indent else 0).let {
                        if (c.columnIndex != colId)
                            it.numberFormat(account.decimalPrecision)
                    }

                    if (rowNum % 2 == 0) {
                        ExcelUtil.fillLong(c, Template.theme.second)
                    }
                }
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

        ExcelUtil.createWorksheetIfNotExists(path, callback = { sht ->

            shtOverview = sht
            heading = Template.heading(sht.workbook)
            light = Template.rowLight(sht.workbook)
            dark = Template.rowDark(sht.workbook)

            sht.createRow(0).apply {
                createCell(colId).setCellValue(titleID)
                createCell(colName).setCellValue(titleName)
                createCell(colOriginal).setCellValue(titleOriginal)
                this@ProtoReporting.categories.forEach {
                    createCell(col++).setCellValue(it.name)
                }
                createCell(col).setCellValue(titleFinal)
            }

            this.structure.forEach {
                writeAccountToXl(it, sht)
            }

            sht.getRow(0).apply {
                this.forEach {
                    ExcelUtil.Update(it).style(heading!!)
                    sht.setColumnWidth(it.columnIndex, 4000)
                }
                heightInPoints = 50f
            }
        })

        cnt = startRow

        var bookings = listOf<Map<Int, String>>()
        val colVal = 3

        ExcelUtil.createWorksheetIfNotExists(path, "adj", { shtCat ->
            bookings = this.categories.fold(listOf<Map<Int, String>>()) { acc, e ->
                val data = mutableMapOf<Int, String>()
                e.entries.forEach {
                    it.accounts.forEach { acc ->
                        shtCat.createRow(cnt++).apply {
                            this.createCell(0).setCellValue(it.category.id.toDouble())
                            this.createCell(1).setCellValue(acc.id.toDouble())
                            this.createCell(2).setCellValue(acc.name)

                            this.createCell(colVal).run {
                                setCellValue(acc.displayValue)
                                ExcelUtil.Update(this).dataFormat(ExcelUtil.DataFormat.NUMBER.format)
                            }

                            this.createCell(4).setCellValue(it.desc)

                            if (data.containsKey(acc.id)) {
                                data[acc.id] = "${data[acc.id]}+'${shtCat.sheetName}'!${CellUtil.getCell(this, colVal).address}"
                            } else {
                                data[acc.id] = "'${shtCat.sheetName}'!${CellUtil.getCell(this, colVal).address}"
                            }
                        }
                    }

                    shtCat.createRow(cnt++)
                }
                acc + listOf(data)
            }
        })

        col = colOriginal + 1

        bookings.forEach {
            ExcelUtil.unload(it, { if (ExcelUtil.digitRegex.matches(it)) it.toDouble().toInt() else -1 }, 0, col++, shtOverview!!, { false }, { c, v ->
                c.cellFormula = v
                //  ExcelUtil.Update(c).numberFormat(GlobalConfiguration.DEFAULT_DECIMAL_PRECISION)
            }, path)
        }
    }
}