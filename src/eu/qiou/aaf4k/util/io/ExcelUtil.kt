package eu.qiou.aaf4k.util.io

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

                val stream = FileOutputStream(path)
                workbook.write(stream)

                stream.flush()
                stream.close()
                workbook.close()
            }
        }

        fun createWorksheetIfNotExists(path: String, sheetName: String = "src", callback: (Sheet) -> Unit){

            if(fileExists(path)){

                processWorksheet(path, sheetName = sheetName, callback = callback)

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
                    "Boolean" to "#"
            )
        }

        fun getPredefinedStyle(type:String, cell: Cell) {
            val wb = cell.sheet.workbook
            val createHelper = wb.creationHelper
            val cellStyle = wb.createCellStyle()
            cellStyle.dataFormat = createHelper.createDataFormat().getFormat(styles().getValue(type))
            cell.cellStyle = cellStyle
        }

        fun setCellFormatAndValue(cell:Cell, value: Any){
            when{
                value is Int -> {
                    cell.setCellValue(value.toDouble())
                    getPredefinedStyle("Int", cell)
                }
                value is Double -> {
                    cell.setCellValue(value.toDouble())
                    getPredefinedStyle("Number", cell)
                    cell.setCellType(CellType.NUMERIC)
                }
                value is Boolean -> {
                    cell.setCellValue(value)
                    getPredefinedStyle("Boolean", cell)
                    cell.setCellType(CellType.BOOLEAN)
                }
                value is Date  -> {
                    cell.setCellValue(value)
                    getPredefinedStyle("Date", cell)
                    cell.setCellType(CellType.NUMERIC)
                }
                value is Calendar -> {
                    cell.setCellValue(value)
                    getPredefinedStyle("Date", cell)
                }
                value is LocalDate -> {
                    cell.setCellValue(java.sql.Date.valueOf(value))
                    getPredefinedStyle("Date", cell)
                }
                else -> {
                    cell.setCellValue(value.toString())
                    cell.setCellType(CellType.STRING)
                }
            }
        }

        fun writeData(path: String, sheetName: String = "src", data: Map<String, List<Any>>, startRow : Int = 0, startCol : Int = 0){
            val f: (Sheet) -> Unit = {
                var r = startRow
                data.forEach { t, u ->
                    val row = it.createRow(r++)
                    var c = startCol

                    u.forEach { i->
                        setCellFormatAndValue(row.createCell(c++) ,i)
                    }
                }
            }

            createWorksheetIfNotExists(path, sheetName, f)
        }

    }
