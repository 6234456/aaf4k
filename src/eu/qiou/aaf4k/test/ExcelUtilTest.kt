package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.template.Template
import org.junit.Test
import java.time.LocalDate


class ExcelUtilTest {

    @Test
    fun writeData() {
        ExcelUtil.writeData("src/eu/qiou/aaf4k/test/demo1.xls", data = mapOf("1" to listOf(23.4, LocalDate.now()), "2" to listOf(234.123123)))
    }

    @Test
    fun formatExcel() {
        ExcelUtil.createWorksheetIfNotExists("src/eu/qiou/aaf4k/test/demo1.xls", "Demo6", {
            with(it.createRow(0).createCell(3)) {
                this.cellStyle = ExcelUtil.StyleBuilder(this.sheet.workbook).fromStyle(Template.heading(this.sheet.workbook)).dataFormat(ExcelUtil.DataFormat.NUMBER).build()
                ExcelUtil.setCellValue(this, 123456)
            }
        })
    }
}