package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.reportings.GlobalConfiguration.DEFAULT_FONT_NAME
import eu.qiou.aaf4k.util.strings.times
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.*

object ExcelUtil {
    val digitRegex = """[-.\d]+""".toRegex()
    val formatSpecifier = """(?:([0#]*)|(?:([0#]{1,3})?(,(?:[0#]{3})?)*))(\.)?([0#]*)""".toRegex()


    // the float numbers in the spreadsheet should be formatted, or by default in will be parsed to Int
    fun parseXlFormatString(s: String): (Number) -> String {
        if (s == "General")
            return parseXlFormatString("#")

        formatSpecifier.matchEntire(s)?.groups?.let {
            val indexOfSharp = it.last()!!.value.indexOfFirst { it == '#' }
            val decimal = with(it.last()!!.range) { this.last - this.first + 1 }
            val thousandSep = it[2] != null || it[3] != null
            val hasDecimalPoint = it[it.size - 2] != null

            return {
                val p1 = String.format("%${if (thousandSep) "," else ""}.${decimal}f", it.toDouble())

                if (indexOfSharp == -1) {
                    p1 + if (hasDecimalPoint && decimal == 0) "." else ""
                } else {
                    val pos = with(p1.takeLast(decimal - indexOfSharp)) { length - (1 + indexOfLast { it1 -> it1 != '0' }) }
                    p1.dropLast(if (pos == decimal) pos + 1 else pos)
                }
            }
        }

        throw Exception("Malformed pattern: \"$s\"")

    }

    fun getWorkbook(path: String): Pair<Workbook, FileInputStream> {
        val inputStream = FileInputStream(path)

        return (if (path.endsWith(".xls"))
            HSSFWorkbook(inputStream)
        else
            XSSFWorkbook(inputStream)) to inputStream
    }

    fun processWorkbook(path: String, callback: (Workbook) -> Unit) {
        val (wb, inputStream) = getWorkbook(path)

        callback(wb)
        inputStream.close()
    }

    fun getWorksheet(path: String, sheetIndex: Int = 0, sheetName: String? = null): Pair<Sheet, FileInputStream> {
        val (wb, inputStream) = getWorkbook(path)

        return (if (sheetName != null)
            wb.getSheet(sheetName)
        else
            wb.getSheetAt(sheetIndex)) to inputStream
    }

    fun getFirstNonEmptyRowNum(sht: Sheet): Int {
        return sht.rowIterator().asSequence().first {
            it.cellIterator().asSequence().any { cell -> textValue(cell).isNotEmpty() }
        }.rowNum
    }

    fun getColNum(sht: Sheet): Int {
        return sht.rowIterator().asSequence().reduce { acc, row ->
            if (row.lastCellNum > acc.lastCellNum) row else acc
        }.lastCellNum.toInt()
    }

    fun processWorksheet(path: String, sheetIndex: Int = 0, sheetName: String? = null, callback: (Sheet) -> Unit) {
        val f: (Workbook) -> Unit = { wb ->

            val sht = if (sheetName != null)
                wb.getSheet(sheetName)
            else
                wb.getSheetAt(sheetIndex)

            callback(sht)
        }
        processWorkbook(path, f)
    }

    fun loopThroughRows(path: String, sheetIndex: Int = 0, sheetName: String? = null, callback: (Row) -> Unit) {
        val f: (Sheet) -> Unit = { sht ->

            val rows = sht.rowIterator()

            while (rows.hasNext()) {
                val row = rows.next()
                callback(row)
            }
        }
        processWorksheet(path, sheetIndex, sheetName, f)
    }

    private fun fileExists(path: String): Boolean {
        val f = File(path)
        return f.exists() && !f.isDirectory
    }

    private fun createWorkbookIfNotExists(path: String, callback: (Workbook) -> Unit = {}) {

        if (fileExists(path)) {
            processWorkbook(path, callback)
        } else {

            val workbook = if (path.endsWith(".xls"))
                HSSFWorkbook().apply {
                    if (summaryInformation == null) {
                        this.createInformationProperties()
                    }
                    summaryInformation.author = GlobalConfiguration.DEFAULT_AUTHOR_NAME
                }
            else
                XSSFWorkbook().apply {
                    properties.coreProperties.creator = GlobalConfiguration.DEFAULT_AUTHOR_NAME
                }

            callback(workbook)
            saveWorkbook(path, workbook)
        }
    }

    fun saveWorkbook(path: String, workbook: Workbook) {
        val stream = FileOutputStream(path)
        workbook.write(stream)

        stream.flush()
        stream.close()
        workbook.close()
    }

    fun existsWorksheet(wb: Workbook, sheetName: String): Boolean {
        return (0 until wb.numberOfSheets).map { wb.getSheetName(it) }.any { it.equals(sheetName) }
    }

    fun createWorksheetIfNotExists(path: String, sheetName: String = "src", callback: (Sheet) -> Unit, readOnly: Boolean = false) {
        if(fileExists(path)){
            processWorkbook(path) { wb ->
                if (existsWorksheet(wb, sheetName))
                    callback(wb.getSheet(sheetName))
                else
                    callback(wb.createSheet(sheetName))

                if (!readOnly) {
                    saveWorkbook(path, wb)
                }
            }
        } else {
            val f: (Workbook) -> Unit = {
                callback(
                        it.createSheet(sheetName)
                )
            }
            createWorkbookIfNotExists(path, f)
        }
    }

    fun <K, V> unload(data: Map<K, V>, operation: (String) -> K, keyCol: Int, targCol: Int, exitCondition: (Row) -> Boolean, processCell: (Cell, V) -> Unit, sheet: Sheet) {

        val callback: (Sheet) -> Unit = { sht ->
            val rows = sht.rowIterator()

            while (rows.hasNext()) {
                val row = rows.next()

                if (exitCondition(row)) {
                    break
                }

                val k = operation(
                        try {
                            textValue(row.getCell(keyCol))
                        } catch (e: IllegalStateException) {
                            ""
                        }
                )

                if (data.containsKey(k)) {
                    processCell(row.getCell(targCol) ?: row.createCell(targCol), data.getValue(k))
                }
            }
        }

        callback(sheet)
    }

    fun textValue(c: Cell, type: CellType = c.cellTypeEnum): String {
        return when (type) {
            CellType.BLANK, CellType.ERROR, CellType._NONE -> ""
            CellType.NUMERIC -> parseXlFormatString(c.cellStyle.dataFormatString)(c.numericCellValue)
            CellType.BOOLEAN -> c.booleanCellValue.toString()
            CellType.STRING -> c.stringCellValue
            CellType.FORMULA -> textValue(c, c.cachedFormulaResultTypeEnum)
        }
    }

    fun longToRGB(l: Long): Triple<Int, Int, Int> {
        val r = l % 256
        val g = ((l - r) / 256) % 256
        val b = (((l - r) / 256 - g) / 256) % 256

        return Triple(r.toInt(), g.toInt(), b.toInt())
    }

    /**
     *  Update the style of the cell in place
     */
    class Update(private val cell: Cell) {
        private val wb = cell.sheet.workbook
        private val createHelper = wb.creationHelper
        private var colorIndex: XSSFColor? = null
        private val isXXSF = wb is XSSFWorkbook
        private var black: XSSFColor? = if (isXXSF) XSSFColor(byteArrayOf(0.toByte(), 0.toByte(), 0.toByte()), (wb as XSSFWorkbook).stylesSource.indexedColors) else null

        //poi lib-bug  use cellutil will erase fore/backgroundColor by XSSF-rgb color
        //https://bz.apache.org/bugzilla/show_bug.cgi?id=59442
        fun prepare(): Update {
            if (isXXSF) {
                colorIndex = (cell.cellStyle as XSSFCellStyle).fillForegroundXSSFColor
            }

            return this
        }

        fun restore(): Update {
            if (isXXSF) {
                if (colorIndex != null && colorIndex != black) {
                    (cell.cellStyle as XSSFCellStyle).run {
                        setFillForegroundColor(colorIndex)
                    }
                }
            }

            return this
        }

        fun style(style: CellStyle, deepCopy: Boolean = false): Update {
            cell.cellStyle =
                    if (!deepCopy) style
                    else StyleBuilder(wb).fromStyle(style).build()

            return this
        }

        fun dataFormat(format: String?): Update {
            if (format != null)
                CellUtil.setCellStyleProperty(cell, CellUtil.DATA_FORMAT, createHelper.createDataFormat().getFormat(format))

            return this
        }

        fun numberFormat(posAfterDecimal: Int? = 2, thousandSep: Boolean = true): Update {
            if (posAfterDecimal == null)
                return this

            return dataFormat((if (thousandSep) "#,##0." else "0.") + "0" * posAfterDecimal)
        }

        fun font(name: String = DEFAULT_FONT_NAME, size: Short = 11, color: Short = IndexedColors.BLACK.index, bold: Boolean = false, italic: Boolean = false, strikeout: Boolean = false, underline: Byte = 0): Update {
            CellUtil.setFont(cell, wb.createFont().apply {
                this.color = color
                this.fontName = name
                this.bold = bold
                this.italic = italic
                this.strikeout = strikeout
                this.underline = underline
                this.fontHeightInPoints = size
            })

            return this
        }

        fun fill(color: Short? = IndexedColors.WHITE.index, style: FillPatternType? = FillPatternType.SOLID_FOREGROUND): Update {
            color?.let {
                CellUtil.setCellStyleProperty(cell, CellUtil.FILL_FOREGROUND_COLOR, color)
            }
            style?.let {
                CellUtil.setCellStyleProperty(cell, CellUtil.FILL_PATTERN, style)
            }

            return this
        }

        fun fillRGB(rgb: Triple<Int, Int, Int>, style: FillPatternType? = FillPatternType.SOLID_FOREGROUND): Update {

            if (wb is HSSFWorkbook) {
                wb.customPalette.setColorAtIndex(StyleBuilder.get(rgb), rgb.first.toByte(), rgb.second.toByte(), rgb.third.toByte())
                return fill(StyleBuilder.get(rgb), style)
            }

            (cell.cellStyle as XSSFCellStyle).setFillForegroundColor(XSSFColor(byteArrayOf(rgb.first.toByte(), rgb.second.toByte(), rgb.third.toByte()), (wb as XSSFWorkbook).stylesSource.indexedColors))
            style?.let {
                cell.cellStyle.setFillPattern(style)
            }
            return this
        }

        fun fillLong(l: Long, style: FillPatternType? = FillPatternType.SOLID_FOREGROUND): Update {
            return fillRGB(longToRGB(l), style)
        }

        fun borderStyle(up: BorderStyle? = null, right: BorderStyle? = null, down: BorderStyle? = null, left: BorderStyle? = null): Update {
            if (up != null) CellUtil.setCellStyleProperty(cell, CellUtil.BORDER_TOP, up)
            if (right != null) CellUtil.setCellStyleProperty(cell, CellUtil.BORDER_RIGHT, right)
            if (down != null) CellUtil.setCellStyleProperty(cell, CellUtil.BORDER_BOTTOM, down)
            if (left != null) CellUtil.setCellStyleProperty(cell, CellUtil.BORDER_LEFT, left)

            return this
        }

        fun borderColor(up: Short? = null, right: Short? = null, down: Short? = null, left: Short? = null): Update {
            if (up != null) CellUtil.setCellStyleProperty(cell, CellUtil.TOP_BORDER_COLOR, up)
            if (right != null) CellUtil.setCellStyleProperty(cell, CellUtil.RIGHT_BORDER_COLOR, right)
            if (down != null) CellUtil.setCellStyleProperty(cell, CellUtil.BOTTOM_BORDER_COLOR, down)
            if (left != null) CellUtil.setCellStyleProperty(cell, CellUtil.LEFT_BORDER_COLOR, left)

            return this
        }

        fun alignment(horizontal: HorizontalAlignment? = null, vertical: VerticalAlignment? = null): Update {
            if (horizontal != null) CellUtil.setCellStyleProperty(cell, CellUtil.ALIGNMENT, horizontal)
            if (vertical != null) CellUtil.setCellStyleProperty(cell, CellUtil.VERTICAL_ALIGNMENT, vertical)

            return this
        }

        fun indent(level: Int? = null): Update {
            if (level != null)
                CellUtil.setCellStyleProperty(cell, CellUtil.INDENTION, level.toShort())

            return this
        }

        fun value(value: Any?): Update {
            if (value != null)
                setCellValue(this.cell, value)

            return this
        }

        fun formula(formula: String?): Update {
            if (formula != null)
                this.cell.cellFormula = with(formula.trim()) {
                    if (this.startsWith("=")) this.substring(1) else this
                }

            return this
        }
    }

    enum class DataFormat(val format: String) {
        DATE("mmm dd, yyyy"),
        NUMBER("#,##0.00"),
        BOOLEAN("#"),
        INT("#.#"),
        DEFAULT("#"),
        STRING("#")
    }

    //due to the limitation of HSSF, font should be possibly re-used
    //if defaultFont is set, it will be applied without creating a new instance
    class StyleBuilder(private val wb: Workbook) {

        private val createHelper = wb.creationHelper
        private var cellStyle = wb.createCellStyle()
        private var multilines: Int = 1

        fun fromStyle(style: CellStyle, fontDeepCopy: Boolean = true): StyleBuilder {
            val font = wb.getFontAt(style.fontIndex)
            cellStyle = with(StyleBuilder(wb)
                    .alignment(style.alignmentEnum, style.verticalAlignmentEnum)
                    .borderStyle(style.borderTopEnum, style.borderRightEnum, style.borderBottomEnum, style.borderLeftEnum)
                    .borderColor(style.topBorderColor, style.rightBorderColor, style.bottomBorderColor, style.leftBorderColor)
                    .dataFormat(format = style.dataFormatString)
                    .indent(level = style.indention.toInt())
                    .multiLineInCell(style.wrapText)) {
                if (wb is HSSFWorkbook)
                    this.fill(color = style.fillForegroundColor, style = style.fillPatternEnum)
                else
                    this.fillColor(
                            (style as XSSFCellStyle).fillForegroundXSSFColor
                            , style = style.fillPatternEnum
                    )

                if (!fontDeepCopy)
                    this.fontObj(font)
                else
                    this.font(name = font.fontName, size = font.fontHeightInPoints,
                            color = font.color, bold = font.bold,
                            italic = font.italic, strikeout = font.strikeout,
                            underline = font.underline)
            }
                    .build()

            return this
        }

        fun dataFormat(format: String?): StyleBuilder {
            if (format != null)
                cellStyle.dataFormat = createHelper.createDataFormat().getFormat(format)
            return this
        }

        fun dataFormat(format: DataFormat): StyleBuilder {
            return this.dataFormat(format.format)
        }

        fun font(name: String = DEFAULT_FONT_NAME, size: Short = 11, color: Short = IndexedColors.BLACK.index, bold: Boolean = false, italic: Boolean = false, strikeout: Boolean = false, underline: Byte = 0): StyleBuilder {
            cellStyle.setFont(buildFont(name, size, color, bold, italic, strikeout, underline))
            return this
        }

        fun fontObj(f: Font? = null): StyleBuilder {
            if (f != null)
                cellStyle.setFont(f)
            return this
        }

        fun buildFont(name: String = DEFAULT_FONT_NAME, size: Short = 11, color: Short = IndexedColors.BLACK.index, bold: Boolean = false, italic: Boolean = false, strikeout: Boolean = false, underline: Byte = 0): Font {
            return wb.createFont().apply {
                this.color = color
                this.fontName = name
                this.bold = bold
                this.italic = italic
                this.strikeout = strikeout
                this.underline = underline
                this.fontHeightInPoints = size
            }
        }

        fun buildFontFrom(f: Font, name: String? = null, size: Short? = null, color: Short? = null, bold: Boolean? = null, italic: Boolean? = null, strikeout: Boolean? = null, underline: Byte? = null): Font {
            return buildFont(name = name ?: f.fontName, size = size ?: f.fontHeightInPoints, color = color ?: f.color,
                    bold = bold ?: f.bold, italic = italic ?: f.italic,
                    strikeout = strikeout ?: f.strikeout, underline = underline ?: f.underline)
        }

        fun buildFontFrom(style: CellStyle, name: String? = null, size: Short? = null, color: Short? = null, bold: Boolean? = null, italic: Boolean? = null, strikeout: Boolean? = null, underline: Byte? = null): Font {
            val f = wb.getFontAt(style.fontIndex)
            return buildFontFrom(f, name, size, color, bold, italic, strikeout, underline)
        }

        fun indent(level: Int? = 0): StyleBuilder {
            if (level != null)
                cellStyle.indention = level.toShort()

            return this
        }

        fun fill(color: Short = IndexedColors.WHITE.index, style: FillPatternType = FillPatternType.SOLID_FOREGROUND): StyleBuilder {
            cellStyle.fillForegroundColor = color
            cellStyle.setFillPattern(style)

            return this
        }

        fun fill(l: Long, style: FillPatternType = FillPatternType.SOLID_FOREGROUND): StyleBuilder {
            return fillRGB(longToRGB(l), style)
        }

        fun fillRGB(rgb: Triple<Int, Int, Int>, style: FillPatternType = FillPatternType.SOLID_FOREGROUND): StyleBuilder {

            if (wb is HSSFWorkbook) {
                wb.customPalette.setColorAtIndex(get(rgb), rgb.first.toByte(), rgb.second.toByte(), rgb.third.toByte())
                return fill(get(rgb), style)
                //return fill(wb.customPalette.findSimilarColor(rgb.first, rgb.second, rgb.third).index, style)
            }

            (cellStyle as XSSFCellStyle).setFillForegroundColor(XSSFColor(byteArrayOf(rgb.first.toByte(), rgb.second.toByte(), rgb.third.toByte()), (wb as XSSFWorkbook).stylesSource.indexedColors))
            cellStyle.setFillPattern(style)
            return this
        }

        fun fillLong(l: Long, style: FillPatternType = FillPatternType.SOLID_FOREGROUND): StyleBuilder {
            return fillRGB(longToRGB(l), style)
        }

        private fun fillColor(color: XSSFColor?, style: FillPatternType): StyleBuilder {
            color?.let {
                (cellStyle as XSSFCellStyle).setFillForegroundColor(color)
                cellStyle.setFillPattern(style)
            }
            return this
        }

        fun borderStyle(up: BorderStyle? = null, right: BorderStyle? = null, down: BorderStyle? = null, left: BorderStyle? = null): StyleBuilder {
            if (up != null) cellStyle.setBorderTop(up)
            if (right != null) cellStyle.setBorderRight(right)
            if (down != null) cellStyle.setBorderBottom(down)
            if (left != null) cellStyle.setBorderLeft(left)

            return this
        }

        fun borderColor(up: Short? = null, right: Short? = null, down: Short? = null, left: Short? = null): StyleBuilder {
            if (up != null) cellStyle.topBorderColor = up
            if (right != null) cellStyle.rightBorderColor = right
            if (down != null) cellStyle.bottomBorderColor = down
            if (left != null) cellStyle.leftBorderColor = left

            return this
        }

        fun alignment(horizontal: HorizontalAlignment? = null, vertical: VerticalAlignment? = null): StyleBuilder {

            if (horizontal != null) cellStyle.setAlignment(horizontal)
            if (vertical != null) cellStyle.setVerticalAlignment(vertical)

            return this
        }

        fun multiLineInCell(multiline: Boolean, lines: Int = 2): StyleBuilder {
            cellStyle.wrapText = multiline

            multilines = if (multiline)
                lines
            else
                1

            return this
        }

        fun build(): CellStyle {
            return cellStyle
        }

        fun applyTo(cell: Cell) {
            cell.cellStyle = this.cellStyle
            cell.row.heightInPoints = cell.row.sheet.defaultRowHeightInPoints * this.multilines
        }

        fun applyTo(cells: Iterable<Cell>) {
            cells.forEach { applyTo(it) }
        }


        companion object {

            // facilitate the rgb-color setting in xls
            private val replaceableHSSFColors = listOf(
                    HSSFColor.OLIVE_GREEN.index, HSSFColor.GREEN.index, HSSFColor.BRIGHT_GREEN.index, HSSFColor.SEA_GREEN.index
            )

            private var index = 0
            private val lastIndex = replaceableHSSFColors.size
            private val m = mutableMapOf<Triple<Int, Int, Int>, Short>()

            fun reset() {
                m.clear()
                index = 0
            }

            fun get(rgb: Triple<Int, Int, Int>): Short {

                return if (m.containsKey(rgb)) {
                    m[rgb]!!
                } else {
                    if (index == lastIndex) {
                        throw Exception("All the replaceable slots are taken!")
                    }
                    m[rgb] = replaceableHSSFColors[index++]
                    get(rgb)
                }
            }
        }
    }

    fun setCellValue(cell: Cell, value: Any) = when (value) {
        is Int -> {
            cell.setCellValue(value.toDouble())
        }
        is Double -> {
            cell.setCellValue(value.toDouble())
            cell.setCellType(CellType.NUMERIC)
        }
        is Boolean -> {
            cell.setCellValue(value)
            cell.setCellType(CellType.BOOLEAN)
        }
        is Date -> {
            cell.setCellValue(value)
            cell.setCellType(CellType.NUMERIC)
        }
        is Calendar -> {
            cell.setCellValue(value)
            cell.setCellType(CellType.NUMERIC)
        }
        is LocalDate -> {
            cell.setCellValue(java.sql.Date.valueOf(value))
            cell.setCellType(CellType.NUMERIC)
        }
        else -> {
            cell.setCellValue(value.toString())
            cell.setCellType(CellType.STRING)
        }
    }


    fun writeData(path: String, sheetName: String = "src", data: Map<*, *>, startRow: Int = 0, startCol: Int = 0, header: Iterable<String>? = null) {
            val f: (Sheet) -> Unit = {
                var r = startRow + if (header == null) 0 else 1

                header?.let { h ->
                    var c = startCol
                    val row = it.createRow(startRow)
                    h.forEach { x ->
                        setCellValue(row.createCell(c++), x)
                    }
                }

                data.forEach { (t, u) ->
                    val row = it.createRow(r++)
                    var c = startCol + 1

                    setCellValue(row.createCell(startCol), t.toString())
                    if (u is Iterable<*>) u.forEach { i ->
                        setCellValue(row.createCell(c++), i!!)
                    }
                    else setCellValue(row.createCell(c), u!!)
                }
            }
            createWorksheetIfNotExists(path, sheetName, f)
        }
    }
