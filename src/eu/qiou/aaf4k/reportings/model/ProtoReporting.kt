package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.foldTrackListInit
import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mergeReduce
import eu.qiou.aaf4k.util.mkJSON
import eu.qiou.aaf4k.util.mkString
import eu.qiou.aaf4k.util.strings.CollectionToString
import eu.qiou.aaf4k.util.strings.times
import eu.qiou.aaf4k.util.template.Template
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellUtil
import java.util.*


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

    //no need to evoke add if specified in constructor
    fun add(category: ProtoCategory<T>) {
        if (categories.any { it.id == category.id })
            throw Exception("Duplicated Category-ID ${category.id} in $categories")

        categories.add(category)
    }

    fun mergeCategories(): Map<Int, Double> {
        return if (categories.isEmpty()) mapOf() else categories.map { it.toDataMap() }.reduce { acc, map ->
            acc.mergeReduce(map) { a, b -> a + b }
        }
    }

    fun checkDuplicate(): Map<Int, Int> {
        return structure
                .fold(listOf<ProtoAccount>()) { acc, t -> acc + t.notStatistical() }
                .fold(mutableMapOf<Int, Int>()) { acc, protoAccount ->
                    if (acc.containsKey(protoAccount.id)) {
                        acc[protoAccount.id] = acc[protoAccount.id]!! + 1
                    } else {
                        acc[protoAccount.id] = 1
                    }
                    acc
                }.filterValues { it > 1 }
    }

    fun shorten(): ProtoReporting<T> {
        val whiteList = categories.fold(flatten()) { acc, protoCategory ->
            acc + protoCategory.flatten()
        }.filter { it.value != 0L }.toSet()

        return ProtoReporting(id, name, desc,
                structure.map { it.shorten(whiteList = whiteList) as T }
                , displayUnit, entity, timeParameters).apply {
            this@ProtoReporting.categories.forEach { it.deepCopy(this) }
        }
    }

    // set all element to 0
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

    fun toXl(path: String, t: Template.Theme = Template.Theme.DEFAULT, locale: Locale = GlobalConfiguration.DEFAULT_LOCALE
    ) {

        val msg = ResourceBundle.getBundle("aaf4k", locale)

        val titleID: String = msg.getString("accountId")
        val titleName: String = msg.getString("accountName")
        val titleOriginal: String = msg.getString("balanceBeforeAdj")
        val titleFinal: String = msg.getString("balanceAfterAdj")
        val prefixStatistical: String = " ${msg.getString("thereOf")}: "

        val categoryID = msg.getString("categoryId")
        val categoryName = msg.getString("categoryName")
        val descStr = msg.getString("desc")
        val amount = msg.getString("amount")


        val startRow = 1
        var cnt = startRow
        val colId = 0
        val colName = colId + 1
        val colOriginal = colName + 1
        val colLast = colOriginal + categories.size + 1
        var col = colOriginal + 1
        var light: CellStyle? = null
        var dark: CellStyle? = null
        var fontBold: Font? = null
        var fontNormal: Font? = null


        fun writeAccountToXl(account: ProtoAccount, sht: Sheet, indent: Int = 0) {

            val l = account.countRecursively(true)
            val lvl = account.levels()

            col = colOriginal + 1

            sht.createRow(cnt++).apply {
                createCell(colId, CellType.STRING).setCellValue(account.id.toString())
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
                        //a sum account can not only contain the statistical children
                        val tmp = account.subAccounts!!.foldTrackListInit(0) { a, protoAccount, _ ->
                            a + protoAccount.countRecursively(true)
                        }.dropLast(1).zip(account.subAccounts.map { !it.isStatistical })

                        createCell(colOriginal).cellFormula = tmp.filter { it.second }.map {
                            CellUtil.getCell(CellUtil.getRow(this.rowNum + 1 + it.first, sht), colOriginal).address
                        }.mkString("+", prefix = "", affix = "")

                        this@ProtoReporting.categories.forEach { _ ->
                            createCell(col).cellFormula = tmp.filter { it.second }.map {
                                CellUtil.getCell(CellUtil.getRow(this.rowNum + 1 + it.first, sht), col).address
                            }.mkString("+", prefix = "", affix = "")
                            col++
                        }
                        col = colOriginal + 1

                    }
                } else
                    createCell(colOriginal).setCellValue(account.displayValue)

                createCell(colLast).cellFormula = "SUM(${getCell(colOriginal).address}:${(getCell(colLast - 1)
                        ?: createCell(colLast - 1)).address})"

                colId.until(colLast + 1).forEach { i ->
                    val c = getCell(i) ?: createCell(i, CellType.NUMERIC)

                    if (rowNum >= 3) {
                        ExcelUtil.Update(c).style(sht.getRow(rowNum - 2).getCell(i).cellStyle)
                    } else {
                        ExcelUtil.StyleBuilder(sht.workbook).fromStyle(if (rowNum % 2 == 1) light!! else dark!!, false)
                                .dataFormat("#,##0.${"0" * account.decimalPrecision}")
                                .fontObj(if (c.columnIndex == colLast) fontBold!! else null)
                                .borderStyle(
                                        right = if (c.columnIndex == colLast) BorderStyle.MEDIUM else null,
                                        left = if (c.columnIndex == colId) BorderStyle.MEDIUM else null
                                )
                                .alignment(if (c.columnIndex == colId) HorizontalAlignment.RIGHT else null)
                                .applyTo(c)
                    }

                    if (c.columnIndex == colName) {
                        ExcelUtil.Update(c).prepare().indent(indent).restore()
                    }

                }
            }

            account.subAccounts?.let {
                it.forEach {
                    writeAccountToXl(it, sht, indent + 1)
                }

                if (l >= 1 && account.levels() == 2) {
                    sht.groupRow(cnt - l + 1, cnt - 1)
                }
            }
        }

        ExcelUtil.createWorksheetIfNotExists(path, callback = { sht ->
            val w = sht.workbook
            val heading = Template.heading(w, t)
            light = Template.rowLight(w, t)
            dark = Template.rowDark(w, t)
            fontNormal = ExcelUtil.StyleBuilder(w).buildFontFrom(light!!)
            fontBold = ExcelUtil.StyleBuilder(w).buildFontFrom(fontNormal!!, bold = true)

            sht.isDisplayGridlines = false

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
                    ExcelUtil.Update(it).style(ExcelUtil.StyleBuilder(w)
                            .fromStyle(heading, false)
                            .borderStyle(
                                    right = if (it.columnIndex == colLast) BorderStyle.MEDIUM else null,
                                    left = if (it.columnIndex == colId) BorderStyle.MEDIUM else null
                            )
                            .build())
                    sht.setColumnWidth(it.columnIndex, 4000)
                }
                heightInPoints = 50f
            }

            //booking
            cnt = startRow

            var bookings = listOf<Map<Int, String>>()
            val colVal = 3

            val bookingCallback: (Sheet) -> Unit = { shtCat ->
                shtCat.isDisplayGridlines = false
                shtCat.createRow(0).apply {
                    createCell(0).setCellValue(categoryID)
                    createCell(1).setCellValue(titleID)
                    createCell(2).setCellValue(titleName)
                    createCell(colVal).setCellValue(amount)
                    createCell(4).setCellValue(descStr)
                    createCell(5).setCellValue(categoryName)

                    ExcelUtil.StyleBuilder(w).fromStyle(heading, false).applyTo(
                            0.until(6).map { i ->
                                shtCat.setColumnWidth(i, 4000)
                                getCell(i)
                            }
                    )

                    heightInPoints = 50f
                }

                val bookingFormat = ExcelUtil.StyleBuilder(w).fromStyle(dark!!, false)
                        .dataFormat(ExcelUtil.DataFormat.NUMBER.format)

                bookings = this.categories.fold(listOf<Map<Int, String>>()) { acc, e ->
                    val data = mutableMapOf<Int, String>()
                    e.entries.forEach {
                        it.accounts.forEach { acc ->
                            shtCat.createRow(cnt++).apply {
                                this.createCell(0).setCellValue(it.category.id.toString())
                                this.createCell(1).setCellValue(acc.id.toString())
                                this.createCell(2).setCellValue(acc.name)
                                this.createCell(colVal).setCellValue(acc.displayValue)
                                this.createCell(4).setCellValue(it.desc)
                                this.createCell(5).setCellValue(e.name)

                                bookingFormat.applyTo(
                                        0.until(6).map { i ->
                                            ExcelUtil.Update(this.getCell(i)).alignment(if (i < 2) HorizontalAlignment.RIGHT else null)
                                            this.getCell(i)
                                        }
                                )


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
            }

            bookingCallback(w.createSheet("adj"))

            col = colOriginal + 1
            bookings.forEach {
                ExcelUtil.unload(it, { if (ExcelUtil.digitRegex.matches(it)) it.toDouble().toInt() else -1 }, 0, col++, { false }, { c, v ->
                    c.cellFormula = v
                }, sht)
            }
        })
    }
}