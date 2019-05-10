package eu.qiou.aaf4k.reportings.base

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.foldTrackListInit
import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.mergeReduce
import eu.qiou.aaf4k.util.mkJSON
import eu.qiou.aaf4k.util.mkString
import eu.qiou.aaf4k.util.strings.CollectionToString
import eu.qiou.aaf4k.util.strings.times
import eu.qiou.aaf4k.util.template.Template
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellUtil
import java.util.*


class Reporting(private val core: ProtoCollectionAccount) : ProtoCollectionAccount by core {

    companion object {
        const val ERSTKONS_CAT_ID = 0
        const val FOLGEKONS_CAT_ID = 1
        const val SCHULDKONS_CAT_ID = 2
        const val AEKONS_CAT_ID = 3
        const val ZGE_CAT_ID = 4
        const val ADJ_CAT_ID = 5
        const val RCL_CAT_ID = 6
        private const val PRESERVED_ID = 10
    }

    private var nextCategoryIndex = PRESERVED_ID + 1

    val categories: MutableList<Category> = mutableListOf()
    override val entity: Entity = core.entity ?: GlobalConfiguration.DEFAULT_REPORTING_ENTITY

    override val timeParameters: TimeParameters = core.timeParameters ?: GlobalConfiguration.DEFAULT_TIME_PARAMETERS
    val structure = core.subAccounts

    fun add(category: Category) = categories.add(category.apply {
        id = nextCategoryIndex++
    })

    override var toUpdate = false

    override var cacheList: List<ProtoAccount> = listOf()
    override var cacheAllList: List<ProtoAccount> = listOf()

    private fun mergeCategories(): Map<Long, Double> {
        return if (categories.isEmpty()) mapOf() else categories.map { it.toDataMap() }.reduce { acc, map ->
            acc.mergeReduce(map) { a, b -> a + b }
        }
    }

    fun guessSuperAccount(id: Long): ProtoAccount {
        tailrec fun search(low: Int, high: Int): Int {
            val mid = (low + high) / 2
            val midVal = this.sortedAllList()[mid].id

            return when {
                high - low == 1 -> low
                id > midVal -> search(mid, high)
                else -> search(low, mid)
            }
        }
        return findAccountByID(id) ?: this.sortedAllList()[search(0, this.sortedAllList().size)]
    }

    // entry-Id to entry
    fun toUncompressedDataMap(): Map<Int, Map<Int, Map<Long, Double>>> {
        return categories.map { it.id to it.toUncompressedDataMap() }.toMap()
    }

    fun toDataMap(): Map<Long, Double> {
        return generate().sortedList().map { it.id to it.decimalValue }.toMap()
    }

    fun checkDuplicate(): Map<Long, Int> {
        return structure
                .fold(listOf<ProtoAccount>()) { acc, t -> acc + t.notStatistical() }
                .fold(mutableMapOf<Long, Int>()) { acc, protoAccount ->
                    if (acc.containsKey(protoAccount.id)) {
                        acc[protoAccount.id] = acc[protoAccount.id]!! + 1
                    } else {
                        acc[protoAccount.id] = 1
                    }
                    acc
                }.filterValues { it > 1 }
    }

    // keep all the first level child even with a null value
    fun shorten(): Reporting {
        val whiteList = categories.fold(sortedList()) { acc, protoCategory ->
            acc + protoCategory.flatten(true)
        }.filter { it.value != 0L }.map { it.id }.toSet()

        return Reporting(this.core.shorten(whiteList = whiteList) as ProtoCollectionAccount).apply {
            this@Reporting.categories.forEach { it.deepCopy(this) }
            nextCategoryIndex = this@Reporting.nextCategoryIndex
            consCategoriesAdded = this@Reporting.consCategoriesAdded
            reclAdjCategoriesAdded = this@Reporting.reclAdjCategoriesAdded
        }
    }


    override fun deepCopy(): ProtoAccount {
        return Reporting(core.deepCopy() as CollectionAccount).apply {
            this@Reporting.categories.forEach { it.deepCopy(this) }
            nextCategoryIndex = this@Reporting.nextCategoryIndex
            consCategoriesAdded = this@Reporting.consCategoriesAdded
            reclAdjCategoriesAdded = this@Reporting.reclAdjCategoriesAdded
        }
    }

    /**
     * after update through the categories
     * get the reporting
     */
    fun generate(clearCategories: Boolean = false): Reporting {
        return (deepCopy() as Reporting).apply {
            update(this@Reporting.mergeCategories())
            if (clearCategories) this.clearCategories()
        }
    }

    // including self
    fun findAccountByID(id: Long): ProtoAccount? {
        return binarySearch(id, true)
    }

    override fun nullify(): ProtoAccount {
        return Reporting(core.nullify() as ProtoCollectionAccount)
    }


    fun removeAccount(accountId: Long) {
        val p = findAccountByID(accountId) ?: throw java.lang.Exception("No account found for the id: $accountId.")
        p.superAccounts.forEach { it.remove(p) }
    }

    fun addAccountTo(newAccount: ProtoAccount, index: Int, parentId: Long? = null) {
        if (parentId == null)
            add(newAccount)
        else {
            val p = findAccountByID(parentId) ?: throw java.lang.Exception("No account found for the id: $parentId.")
            if (p is ProtoCollectionAccount) p.add(newAccount, index)
        }
    }


    val periodResultInBalance = sortedList().find { it.reportingType == ReportingType.RESULT_BALANCE }
    val retainedEarning = sortedList().find { it.reportingType == ReportingType.RETAINED_EARNINGS_BEGINNING }
    val oci = sortedList().find { it.reportingType == ReportingType.PROFIT_LOSS_NEUTRAL_BALANCE }
    val diffSchuKons = sortedList().find { it.reportingType == ReportingType.DIFF_CONS_RECEIVABLE_PAYABLE }
    val diffAEKons = sortedList().find { it.reportingType == ReportingType.DIFF_CONS_REVENUE_EXPENSE }

    val categoryInitEquityCons
        get() = categories.find { it.id == ERSTKONS_CAT_ID }
    val categorySubsequentEquityCons
        get() = categories.find { it.id == FOLGEKONS_CAT_ID }
    val categoryPayablesReceivabelsCons
        get() = categories.find { it.id == SCHULDKONS_CAT_ID }
    val categoryRevenueExpenseCons
        get() = categories.find { it.id == AEKONS_CAT_ID }
    val categoryUnrealisedGainCons
        get() = categories.find { it.id == ZGE_CAT_ID }

    fun prepareConsolidation(locale: Locale? = null) {
        if (!consCategoriesAdded && !reclAdjCategoriesAdded) {

            val msg = if (locale == null)
                ResourceBundle.getBundle("aaf4k")
            else
                ResourceBundle.getBundle("aaf4k", locale)

            Category(msg.getString("erstKons"), msg.getString("erstKons"), this, ConsolidationCategory.INIT_EQUITY).apply { id = ERSTKONS_CAT_ID; this@Reporting.nextCategoryIndex-- }
            Category(msg.getString("folgKons"), msg.getString("folgKons"), this, ConsolidationCategory.SUBSEQUENT_EQUITY).apply { id = FOLGEKONS_CAT_ID; this@Reporting.nextCategoryIndex-- }
            Category(msg.getString("schuKons"), msg.getString("schuKons"), this, ConsolidationCategory.PAYABLES_RECEIVABLES).apply { id = SCHULDKONS_CAT_ID; this@Reporting.nextCategoryIndex-- }
            Category(msg.getString("aeKons"), msg.getString("aeKons"), this, ConsolidationCategory.REVENUE_EXPENSE).apply { id = AEKONS_CAT_ID; this@Reporting.nextCategoryIndex-- }
            Category(msg.getString("zwischenGewinnE"), msg.getString("zwischenGewinnE"), this, ConsolidationCategory.UNREALIZED_PROFIT_AND_LOSS).apply { id = ZGE_CAT_ID; this@Reporting.nextCategoryIndex-- }

            consCategoriesAdded = true
        }
    }

    fun prepareReclAdj(locale: Locale? = null) {
        if (!consCategoriesAdded && !reclAdjCategoriesAdded) {
            val msg = if (locale == null)
                ResourceBundle.getBundle("aaf4k")
            else
                ResourceBundle.getBundle("aaf4k", locale)

            Category(msg.getString("adjustment"), msg.getString("adjustment"), this).apply { id == ADJ_CAT_ID; this@Reporting.nextCategoryIndex-- }
            Category(msg.getString("reclassification"), msg.getString("reclassification"), this).apply { id == RCL_CAT_ID; this@Reporting.nextCategoryIndex-- }

            reclAdjCategoriesAdded = true
        }
    }

    fun fx(targetCurrency: CurrencyUnit) {
        if (reclAdjCategoriesAdded && this.unit != targetCurrency) {
            //TODO
        }
    }

    var consCategoriesAdded = false
        private set

    private var reclAdjCategoriesAdded = false

    fun clearCategories() {
        categories.clear()
        consCategoriesAdded = false
        reclAdjCategoriesAdded = false
    }

    fun carryForward(): Reporting {
        return (deepCopy() as Reporting).apply {
            val re = this.retainedEarning!!
            val pl = this.sortedList().filter {
                it.reportingType == ReportingType.REVENUE_GAIN
                        || it.reportingType == ReportingType.EXPENSE_LOSS
                        || it.reportingType == ReportingType.PROFIT_LOSS_NEUTRAL
                        || it.reportingType == ReportingType.AUTO
            }.filter { it.decimalValue != 0.0 }

            Category("", "", this).apply {
                Entry("", this).apply {
                    pl.forEach {
                        add(it.id, it.decimalValue * -1)
                    }
                    balanceWith(re.id)
                }
                summarizeResult()
            }
        }
    }

    override fun toJSON(): String {
        return """{"id":$id, "name":"$name", "desc":"$desc", "core":${CollectionToString.mkJSON(structure)}, "entity":${entity.toJSON()}, "timeParameters":${timeParameters.toJSON()}, "categories":${categories.mkJSON()}}"""
    }

    override fun toString(): String {
        return CollectionToString.mkString(structure)
    }

    fun toXl(path: String,
             t: Template.Theme = Template.Theme.DEFAULT,
             locale: Locale = GlobalConfiguration.DEFAULT_LOCALE,
             shtNameOverview: String = "src",
             shtNameAdjustment: String = "adj",
             components: Map<Entity, Reporting>? = null
    ): Pair<Sheet, Map<Long, String>> {

        val msg = ResourceBundle.getBundle("aaf4k", locale)

        val titleID: String = msg.getString("accountId")
        val titleName: String = msg.getString("accountName")
        val titleOriginal: String = msg.getString("balanceBeforeAdj")
        val titleFinal: String = msg.getString("balanceAfterAdj")
        val prefixStatistical = " ${msg.getString("thereOf")}: "

        val categoryID = msg.getString("categoryId")
        val categoryName = msg.getString("categoryName")
        val descStr = msg.getString("desc")
        val amount = msg.getString("amount")

        val headingHeight = 50f
        val rowHeight = 24f

        val startRow = 1
        var cnt = startRow
        val colId = 0
        val colName = colId + 1
        val colOriginal = colName + 1
        val colLast = colOriginal + categories.size + 1 + (components?.size ?: 0)

        val colSumOriginal = if (components == null) null else colName + 1 + components.size
        var colCategoryBegin = colOriginal + 1 + (components?.size ?: 0)
        var light: CellStyle? = null
        var dark: CellStyle? = null
        var fontBold: Font? = null
        var fontNormal: Font?

        val res: MutableMap<Long, String> = mutableMapOf()

        var res1: Pair<Sheet, Map<Long, String>>? = null


        fun writeAccountToXl(account: ProtoAccount, sht: Sheet, indent: Int = 0) {

            val l = if (account is ProtoCollectionAccount) account.countRecursively(true) else 1
            val lvl = if (account is ProtoCollectionAccount) account.levels() else 1

            colCategoryBegin = colOriginal + 1 + (components?.size ?: 0)

            sht.createRow(cnt++).apply {
                createCell(colId, CellType.STRING).setCellValue(account.id.toString())
                createCell(colName).setCellValue("${if (account.isStatistical) prefixStatistical else ""}${account.name}")

                if (l > 1) {
                    if (lvl == 2) {
                        colOriginal.until(colLast).forEach {
                            if (colSumOriginal == null || colSumOriginal != it) {
                                createCell(it).cellFormula =
                                        "SUM(${CellUtil.getCell(CellUtil.getRow(this.rowNum + 1, sht), it).address}:" +
                                                "${CellUtil.getCell(CellUtil.getRow(this.rowNum + l - 1, sht), it).address}" +
                                                ")"
                            } else {
                                createCell(it).cellFormula = "SUM(${getCell(colOriginal).address}:${(getCell(it - 1)
                                        ?: createCell(it - 1)).address})"
                            }
                        }
                    } else {
                        //a sum account can not only contain the statistical children
                        val tmp = (account as ProtoCollectionAccount).subAccounts.foldTrackListInit(0) { a, protoAccount, _ ->
                            a + if (protoAccount is ProtoCollectionAccount) protoAccount.countRecursively(true) else 1
                        }.dropLast(1).zip(account.subAccounts.map { !it.isStatistical })

                        colOriginal.until(colLast).forEach { x ->
                            if (colSumOriginal == null || colSumOriginal != x) {
                                createCell(x).cellFormula = tmp.filter { it.second }.map {
                                    CellUtil.getCell(CellUtil.getRow(this.rowNum + 1 + it.first, sht), x).address
                                }.mkString("+", prefix = "", affix = "")
                            } else {
                                createCell(x).cellFormula = "SUM(${getCell(colOriginal).address}:${(getCell(x - 1)
                                        ?: createCell(x - 1)).address})"
                            }
                        }
                    }
                } else {
                    createCell(colOriginal).setCellValue(account.displayValue)
                }

                if (components != null) {
                    createCell(colCategoryBegin - 1).cellFormula = "SUM(${(getCell(colCategoryBegin - 2)
                            ?: createCell(colCategoryBegin - 2)).address}:${(getCell(colOriginal)
                            ?: createCell(colOriginal)).address})"
                }
                createCell(colLast).cellFormula = "SUM(${
                (getCell(colSumOriginal ?: colOriginal) ?: createCell(colSumOriginal
                        ?: colOriginal)).address}:${(getCell(colLast - 1)
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

            if (account is ProtoCollectionAccount)
                account.subAccounts.let {
                    it.forEach { x ->
                        writeAccountToXl(x, sht, indent + 1)
                    }

                    sht.groupRow(cnt - l + 1, cnt - 1)
                }
        }

        ExcelUtil.createWorksheetIfNotExists(path, sheetName = shtNameOverview, callback = { sht ->
            val w = sht.workbook
            val heading = Template.heading(w, t)
            light = Template.rowLight(w)
            dark = Template.rowDark(w, t)
            fontNormal = ExcelUtil.StyleBuilder(w).buildFontFrom(light!!)
            fontBold = ExcelUtil.StyleBuilder(w).buildFontFrom(fontNormal!!, bold = true)

            sht.isDisplayGridlines = false

            sht.createRow(0).apply {
                createCell(colId).setCellValue(titleID)
                createCell(colName).setCellValue(titleName)

                if (components == null) {
                    createCell(colOriginal).setCellValue(titleOriginal)
                } else {
                    var cnti = colName + 1
                    components.forEach { (k, _) ->
                        createCell(cnti++).setCellValue(k.name)
                    }
                    createCell(cnti++).setCellValue(titleOriginal)
                }
                this@Reporting.categories.forEach {
                    createCell(colCategoryBegin++).setCellValue(it.name)
                }
                createCell(colLast).setCellValue(titleFinal)
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
                heightInPoints = headingHeight
            }

            sht.iterator().forEach {
                if (it.rowNum > 0)
                    it.heightInPoints = rowHeight
            }

            //booking
            cnt = startRow

            var bookings = listOf<Map<Long, String>>()
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

                    heightInPoints = headingHeight
                }

                val bookingFormat = ExcelUtil.StyleBuilder(w).fromStyle(dark!!, false)
                        .dataFormat(ExcelUtil.DataFormat.NUMBER.format)

                bookings = this.categories.fold(listOf()) { acc, e ->
                    val data = mutableMapOf<Long, String>()
                    e.entries.filter { it.isActive }.forEach {
                        it.accounts.forEach { acc ->
                            shtCat.createRow(cnt++).apply {
                                heightInPoints = rowHeight

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

            bookingCallback(w.createSheet(shtNameAdjustment))

            colCategoryBegin = colOriginal + 1 + (components?.size ?: 0)
            bookings.forEach { x ->
                ExcelUtil.unload(x, { if (ExcelUtil.digitRegex.matches(it)) it.toDouble().toLong() else -1 }, 0, colCategoryBegin++, { false }, { c, v ->
                    c.cellFormula = v
                }, sht)
            }

            sht.rowIterator().forEach { x ->
                if (x.rowNum > 1) {
                    val c = x.getCell(colLast)
                    res[x.getCell(colId).stringCellValue.toLong()] = "'${sht.sheetName}'!${c.address}"
                }
            }

            res1 = sht to res

        })

        return res1!!
    }
}