package eu.qiou.aaf4k.util.io

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.hssf.util.HSSFColor.LAVENDER
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

        private fun styles():Map<String, String> {
            return mapOf(
                    "Int" to "#.#",
                    "Date" to "mmm dd, yyyy",
                    "Number" to "#.00",
                    "Boolean" to "#",
                    "String" to ""
            )
        }

    fun getPredefinedStyle(type: String, cell: Cell, callback: (cellStyle: CellStyle, cell: Cell) -> Unit = { _, _ -> }) {
        val wb = cell.sheet.workbook
        val createHelper = wb.creationHelper
        val cellStyle = wb.createCellStyle()

        cellStyle.dataFormat = createHelper.createDataFormat().getFormat(styles().getValue(type))
        callback(cellStyle, cell)

        cell.cellStyle = cellStyle
    }

    fun setColor(wb: HSSFWorkbook, r: Byte, g: Byte, b: Byte): HSSFColor {
        return with(wb.customPalette) {
            findColor(r, g, b) ?: this.apply { setColorAtIndex(LAVENDER.index, r, g, b) }.getColor(LAVENDER.index)
        }
    }


    fun setCellFormatAndValue(cell: Cell, value: Any, callback: (cellStyle: CellStyle, cell: Cell) -> Unit = { _, _ -> }) {
            when{
                value is Int -> {
                    cell.setCellValue(value.toDouble())
                    getPredefinedStyle("Int", cell, callback)
                }
                value is Double -> {
                    cell.setCellValue(value.toDouble())
                    getPredefinedStyle("Number", cell, callback)
                    cell.setCellType(CellType.NUMERIC)
                }
                value is Boolean -> {
                    cell.setCellValue(value)
                    getPredefinedStyle("Boolean", cell, callback)
                    cell.setCellType(CellType.BOOLEAN)
                }
                value is Date  -> {
                    cell.setCellValue(value)
                    getPredefinedStyle("Date", cell, callback)
                    cell.setCellType(CellType.NUMERIC)
                }
                value is Calendar -> {
                    cell.setCellValue(value)
                    getPredefinedStyle("Date", cell, callback)
                }
                value is LocalDate -> {
                    cell.setCellValue(java.sql.Date.valueOf(value))
                    getPredefinedStyle("Date", cell, callback)
                }
                else -> {
                    cell.setCellValue(value.toString())
                    getPredefinedStyle("String", cell, callback)
                    cell.setCellType(CellType.STRING)
                }
            }
        }

    fun writeData(path: String, sheetName: String = "src", data: Map<String, List<Any>>, startRow: Int = 0, startCol: Int = 0, callback: (cellStyle: CellStyle, cell: Cell) -> Unit = { _, _ -> }) {
            val f: (Sheet) -> Unit = {
                var r = startRow
                data.forEach { t, u ->
                    val row = it.createRow(r++)
                    var c = startCol

                    u.forEach { i->
                        setCellFormatAndValue(row.createCell(c++), i, callback)
                    }
                }
            }
            createWorksheetIfNotExists(path, sheetName, f)
        }
    }
