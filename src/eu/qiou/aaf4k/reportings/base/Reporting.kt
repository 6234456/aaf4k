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
        private const val INIT_KAPCONS_CAT_ID = 0


        private const val PRESERVED_ID = 10
    }

    private var nextCategoryIndex = PRESERVED_ID + 1

    val categories: MutableList<Category> = mutableListOf()
    override val entity: Entity = core.entity ?: GlobalConfiguration.DEFAULT_REPORTING_ENTITY

    override val timeParameters: TimeParameters = core.timeParameters ?: GlobalConfiguration.DEFAULT_TIME_PARAMETERS
    private val structure = core.subAccounts

    fun add(category: Category) = categories.add(category.apply {
        id = nextCategoryIndex++
    })

    override fun add(child: ProtoAccount, index: Int?): ProtoCollectionAccount {
        return this.apply {
            super.add(child, index)
            toUpdate = true
        }
    }

    var toUpdate = false

    override var sortedList: List<ProtoAccount> = flatten()
        get() = if (toUpdate) field else {
            toUpdate = false; flatten()
        }

    override var sortedAllList: List<ProtoAccount> = flattenAll()
        get() = if (toUpdate) field else {
            toUpdate = false; flattenAll()
        }

    private fun mergeCategories(): Map<Long, Double> {
        return if (categories.isEmpty()) mapOf() else categories.map { it.toDataMap() }.reduce { acc, map ->
            acc.mergeReduce(map) { a, b -> a + b }
        }
    }

    fun guessSuperAccount(id: Long): ProtoAccount {
        tailrec fun search(low: Int, high: Int): Int {
            val mid = (low + high) / 2
            val midVal = this.sortedAllList[mid].id

            return when {
                high - low == 1 -> low
                id > midVal -> search(mid, high)
                else -> search(low, mid)
            }
        }
        return findAccountByID(id) ?: this.sortedAllList[search(0, this.sortedAllList.size)]
    }

    // entry-Id to entry
    fun toUncompressedDataMap(): Map<Int, Map<Int, Map<Long, Double>>> {
        return categories.map { it.id to it.toUncompressedDataMap() }.toMap()
    }

    fun toDataMap(): Map<Long, Double> {
        return generate().flattenWithStatistical().map { it.id to it.decimalValue }.toMap()
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

    fun shorten(): Reporting {
        val whiteList = categories.fold(this.sortedList) { acc, protoCategory ->
            acc + protoCategory.flatten(true)
        }.filter { it.value != 0L }.toSet()

        return Reporting(core.shorten(whiteList = whiteList) as ProtoCollectionAccount)
    }

    private fun flattenWithStatistical(): List<ProtoAccount> {
        if (structure.isEmpty())
            return listOf()

        return structure.map { if (it is ProtoCollectionAccount) it.flatten() else mutableListOf(it) }.reduce { acc, mutableList ->
            acc.apply { addAll(mutableList) }
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
            update(mergeCategories())
            if (clearCategories) this.categories.clear()
        }
    }

    // including self
    fun findAccountByID(id: Long): ProtoAccount? {
        return binarySearch(id, true)
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


    val periodResultInBalance = this.sortedList.find { it.reportingType == ReportingType.RESULT_BALANCE }
    val retainedEarning = this.sortedList.find { it.reportingType == ReportingType.RETAINED_EARNINGS_BEGINNING }
    val oci = this.sortedList.find { it.reportingType == ReportingType.PROFIT_LOSS_NEUTRAL_BALANCE }
    val diffSchuKons = this.sortedList.find { it.reportingType == ReportingType.DIFF_CONS_RECEIVABLE_PAYABLE }
    val diffAEKons = this.sortedList.find { it.reportingType == ReportingType.DIFF_CONS_REVENUE_EXPENSE }

    fun prepareConsolidation(locale: Locale? = null) {
        if (!consCategoriesAdded && !reclAdjCategoriesAdded) {

            val msg = if (locale == null)
                ResourceBundle.getBundle("aaf4k")
            else
                ResourceBundle.getBundle("aaf4k", locale)

            Category(msg.getString("erstKons"), msg.getString("erstKons"), this, ConsolidationCategory.INIT_EQUITY)
            Category(msg.getString("folgKons"), msg.getString("folgKons"), this, ConsolidationCategory.SUBSEQUENT_EQUITY)
            Category(msg.getString("schuKons"), msg.getString("schuKons"), this, ConsolidationCategory.PAYABLES_RECEIVABLES)
            Category(msg.getString("aeKons"), msg.getString("aeKons"), this, ConsolidationCategory.REVENUE_EXPENSE)
            Category(msg.getString("zwischenGewinnE"), msg.getString("zwischenGewinnE"), this, ConsolidationCategory.UNREALIZED_PROFIT_AND_LOSS)

            consCategoriesAdded = true
        }
    }

    fun prepareReclAdj(locale: Locale? = null) {
        if (!consCategoriesAdded && !reclAdjCategoriesAdded) {
            val msg = if (locale == null)
                ResourceBundle.getBundle("aaf4k")
            else
                ResourceBundle.getBundle("aaf4k", locale)

            Category(msg.getString("adjustment"), msg.getString("adjustment"), this)
            Category(msg.getString("reclassification"), msg.getString("reclassification"), this)

            reclAdjCategoriesAdded = true
        }
    }

    fun fx(targetCurrency: CurrencyUnit) {
        if (reclAdjCategoriesAdded && this.displayUnit != targetCurrency) {
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
            val pl = this.sortedList.filter {
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

                    if (l >= 1 && account.levels() == 2) {
                        sht.groupRow(cnt - l + 1, cnt - 1)
                    }
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
                heightInPoints = 50f
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

                    heightInPoints = 50f
                }

                val bookingFormat = ExcelUtil.StyleBuilder(w).fromStyle(dark!!, false)
                        .dataFormat(ExcelUtil.DataFormat.NUMBER.format)

                bookings = this.categories.fold(listOf()) { acc, e ->
                    val data = mutableMapOf<Long, String>()
                    e.entries.filter { it.isActive }.forEach {
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