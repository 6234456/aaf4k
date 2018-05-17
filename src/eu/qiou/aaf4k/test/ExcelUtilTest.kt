package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.junit.Test
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.*


class ExcelUtilTest {

    @Test
    fun writeData() {
        ExcelUtil.writeData("src/eu/qiou/aaf4k/test/demo1.xls",data = mapOf("1" to listOf(23.4, LocalDate.now()), "2" to listOf(234.123123)))
    }

    @Test
    fun formatExcel() {
        ExcelUtil.createWorksheetIfNotExists("src/eu/qiou/aaf4k/test/demo1.xls", "Demo3", {
            with(it.createRow(0).createCell(3)) {
                ExcelUtil.setCellFormatAndValue(this, LocalDate.now(), {
                    it.fillForegroundColor = IndexedColors.BLUE_GREY.index
                    it.setFillPattern(FillPatternType.SOLID_FOREGROUND)
                })
            }
        })
    }

    @Test
    fun demo(){
        val wb = HSSFWorkbook()
        //Workbook wb = new XSSFWorkbook();
        val createHelper = wb.creationHelper
        val sheet = wb.createSheet("new sheet")

        // Create a row and put some cells in it. Rows are 0 based.
        val row = sheet.createRow(0)

        // Create a cell and put a date value in it.  The first cell is not styled
        // as a date.
        var cell = row.createCell(0)
        cell.setCellValue(Date())

        // we style the second cell as a date (and time).  It is important to
        // create a new cell style from the workbook otherwise you can end up
        // modifying the built in style and effecting not only this cell but other cells.
        val cellStyle = wb.createCellStyle()
        cellStyle.dataFormat = createHelper.createDataFormat().getFormat("yy-m-d")
        cell = row.createCell(1)
        cell.setCellValue(Date())
        cell.setCellStyle(cellStyle)

        //you can also set date as java.util.Calendar
        cell = row.createCell(2)
        cell.setCellValue(Calendar.getInstance())
        cell.setCellStyle(cellStyle)

        // Write the output to a file
        FileOutputStream("workbook.xls").use { fileOut -> wb.write(fileOut) }
    }
}