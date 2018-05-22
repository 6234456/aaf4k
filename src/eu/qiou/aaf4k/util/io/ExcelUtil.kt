package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.reportings.GlobalConfiguration.DEFAULT_FONT_NAME
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.*

object ExcelUtil {

        fun processWorkbook(path: String, callback: (Workbook)-> Unit){
            val inputStream = FileInputStream(path)

            val wb = if (path.endsWith(".xls"))
                HSSFWorkbook(inputStream)
            else
                XSSFWorkbook(inputStream)

            callback(wb)
            inputStream.close()
        }

        fun processWorksheet(path:String, sheetIndex: Int = 0, sheetName: String? = null, callback: (Sheet) -> Unit){
            val f: (Workbook)-> Unit = {
                wb ->

                val sht = if(sheetName != null)
                    wb.getSheet(sheetName)
                else
                    wb.getSheetAt(sheetIndex)

                callback(sht)
            }
            processWorkbook(path, f)
        }

        fun loopThroughRows(path:String, sheetIndex: Int = 0, sheetName: String? = null, callback: (Row) -> Unit){
            val f: (Sheet)-> Unit = {
                sht ->

                val rows = sht.rowIterator()

                while (rows.hasNext()){
                    val row = rows.next()
                    callback(row)
                }
            }
            processWorksheet(path, sheetIndex, sheetName, f)
        }

        private fun fileExists(path: String):Boolean{
            val f = File(path)
            return f.exists() && !f.isDirectory
        }

        fun createWorkbookIfNotExists(path:String, callback: (Workbook) -> Unit = {}){

            if(fileExists(path)){
                processWorkbook(path, callback)
            } else {

                val workbook =  if (path.endsWith(".xls"))
                    HSSFWorkbook()
                else
                    XSSFWorkbook()

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
        return (0 until wb.numberOfSheets).map { wb.getSheetName(it) }.any({ it.equals(sheetName) })
    }

    fun createWorksheetIfNotExists(path: String, sheetName: String = "src", callback: (Sheet) -> Unit, readOnly: Boolean = false) {
        if(fileExists(path)){
            processWorkbook(path, { wb ->
                if (existsWorksheet(wb, sheetName))
                    callback(wb.getSheet(sheetName))
                else
                    callback(wb.createSheet(sheetName))

                if (!readOnly) {
                    saveWorkbook(path, wb)
                }
            })
        } else {
            val f: (Workbook) -> Unit = {
                callback(
                        it.createSheet(sheetName)
                )
            }
            createWorkbookIfNotExists(path, f)
        }
    }

    class Update(val cell: Cell) {
        private val wb = cell.sheet.workbook
        private val createHelper = wb.creationHelper

        fun style(style: CellStyle): Update {
            cell.cellStyle = style

            return this
        }

        fun dataFormat(format: String?): Update {
            if (format != null)
                cell.cellStyle.also { it.dataFormat = createHelper.createDataFormat().getFormat(format) }

            return this
        }

        fun font(name: String = DEFAULT_FONT_NAME, size: Short = 11, color: Short = IndexedColors.BLACK.index, bold: Boolean = false, italic: Boolean = false, strikeout: Boolean = false, underline: Byte = 0): Update {
            cell.cellStyle.also {
                it.setFont(wb.createFont().apply {
                    this.color = color
                    this.fontName = name
                    this.bold = bold
                    this.italic = italic
                    this.strikeout = strikeout
                    this.underline = underline
                    this.fontHeightInPoints = size
                })
            }

            return this
        }

        fun fill(color: Short = IndexedColors.WHITE.index, style: FillPatternType = FillPatternType.SOLID_FOREGROUND): Update {
            cell.cellStyle.also {
                it.fillForegroundColor = color
                it.setFillPattern(style)
            }

            return this
        }

        fun borderStyle(up: BorderStyle? = null, right: BorderStyle? = null, down: BorderStyle? = null, left: BorderStyle? = null): Update {
            cell.cellStyle.also {
                if (up != null) it.setBorderTop(up)
                if (right != null) it.setBorderRight(right)
                if (down != null) it.setBorderBottom(down)
                if (left != null) it.setBorderLeft(left)
            }

            return this
        }

        fun borderColor(up: Short? = null, right: Short? = null, down: Short? = null, left: Short? = null): Update {
            cell.cellStyle.also {
                if (up != null) it.topBorderColor = up
                if (right != null) it.rightBorderColor = right
                if (down != null) it.bottomBorderColor = down
                if (left != null) it.leftBorderColor = left
            }

            return this
        }

        fun alignment(horizontal: HorizontalAlignment? = null, vertical: VerticalAlignment? = null): Update {
            cell.cellStyle.also {
                if (horizontal != null) it.setAlignment(horizontal)
                if (vertical != null) it.setVerticalAlignment(vertical)
            }

            return this
        }

        fun value(value: Any): Update {
            setCellValue(this.cell, value)

            return this
        }
    }

    enum class DataFormat(val format: String) {
        DATE("mmm dd, yyyy"),
        NUMBER("#.00"),
        BOOLEAN("#"),
        INT("#.#"),
        DEFAULT("#"),
        STRING("#")
    }

    class StyleBuilder(val wb: Workbook) {

        private val createHelper = wb.creationHelper
        private var cellStyle = wb.createCellStyle()
        private var multilines: Int = 1

        fun fromStyle(style: CellStyle): StyleBuilder {
            val font = wb.getFontAt(style.fontIndex)
            cellStyle = StyleBuilder(wb)
                    .alignment(style.alignmentEnum, style.verticalAlignmentEnum)
                    .borderStyle(style.borderTopEnum, style.borderRightEnum, style.borderBottomEnum, style.borderLeftEnum)
                    .borderColor(style.topBorderColor, style.rightBorderColor, style.bottomBorderColor, style.leftBorderColor)
                    .dataFormat(format = style.dataFormatString)
                    .font(name = font.fontName, size = font.fontHeightInPoints,
                            color = font.color, bold = font.bold,
                            italic = font.italic, strikeout = font.strikeout,
                            underline = font.underline)
                    .fill(color = style.fillForegroundColor, style = style.fillPatternEnum)
                    .multiLineInCell(style.wrapText)
                    .build()

            return this
        }

        fun dataFormat(format: String): StyleBuilder {
            cellStyle.dataFormat = createHelper.createDataFormat().getFormat(format)
            return this
        }

        fun dataFormat(format: DataFormat): StyleBuilder {
            return this.dataFormat(format.format)
        }

        fun font(name: String = "Arial", size: Short = 11, color: Short = IndexedColors.BLACK.index, bold: Boolean = false, italic: Boolean = false, strikeout: Boolean = false, underline: Byte = 0): StyleBuilder {
            cellStyle.setFont(wb.createFont().apply {
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

        fun fill(color: Short = IndexedColors.WHITE.index, style: FillPatternType = FillPatternType.SOLID_FOREGROUND): StyleBuilder {
            cellStyle.fillForegroundColor = color
            cellStyle.setFillPattern(style)

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

            if (multiline)
                multilines = lines
            else
                multilines = 1

            return this
        }

        fun build(): CellStyle {
            return cellStyle
        }

        fun applyTo(cell: Cell) {
            cell.cellStyle = this.cellStyle
            cell.row.heightInPoints = cell.row.sheet.defaultRowHeightInPoints * this.multilines
        }
    }

    fun setCellValue(cell: Cell, value: Any) {
        when {
            value is Int -> {
                cell.setCellValue(value.toDouble())
            }
            value is Double -> {
                cell.setCellValue(value.toDouble())
                cell.setCellType(CellType.NUMERIC)
            }
            value is Boolean -> {
                cell.setCellValue(value)
                cell.setCellType(CellType.BOOLEAN)
            }
            value is Date -> {
                cell.setCellValue(value)
                cell.setCellType(CellType.NUMERIC)
            }
            value is Calendar -> {
                cell.setCellValue(value)
                cell.setCellType(CellType.NUMERIC)
            }
            value is LocalDate -> {
                cell.setCellValue(java.sql.Date.valueOf(value))
                cell.setCellType(CellType.NUMERIC)
            }
            else -> {
                cell.setCellValue(value.toString())
                cell.setCellType(CellType.STRING)
            }
        }
    }


    fun writeData(path: String, sheetName: String = "src", data: Map<String, List<Any>>, startRow: Int = 0, startCol: Int = 0) {
            val f: (Sheet) -> Unit = {
                var r = startRow
                data.forEach { t, u ->
                    val row = it.createRow(r++)
                    var c = startCol

                    u.forEach { i->
                        setCellValue(row.createCell(c++), i)
                    }
                }
            }
            createWorksheetIfNotExists(path, sheetName, f)
        }
    }
