package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.template.Template
import org.apache.poi.ss.usermodel.IndexedColors
import org.junit.Test
import java.time.LocalDate


class ExcelUtilTest {

    @Test
    fun writeData() {
        ExcelUtil.writeData("src/eu/qiou/aaf4k/test/demo1.xls", data = mapOf("1" to listOf(23.4, LocalDate.now()), "2" to listOf(234.123123)))
    }

    @Test
    fun formatExcel() {
        Template(
                listOf(
                        Template.HeadingFormat("杨  ", ExcelUtil.DataFormat.STRING.format, ExcelUtil.DataFormat.STRING.format),
                        Template.HeadingFormat(LocalDate.now(), "mmm yyyy", "#,###,")
                ),
                listOf(
                        listOf("Hello", -12345),
                        listOf("Hello2", 12345.98),
                        listOf("Hello3", 1231145),
                        listOf("Hello4", 1231145),
                        listOf("Hello5", 112345),
                        listOf("Hello6", 112345),
                        listOf("Hello7", 123415)
                ),
                colorSchema = Template.ColorSchema(IndexedColors.LIGHT_ORANGE, IndexedColors.GREY_25_PERCENT),
                caption = listOf(
                        Pair("Demo of the Excel-Format", "QIY"),
                        Pair(LocalDate.now().toString(), "001")
                ),
                sumColRight = Template.HeadingFormat("Total", formatData = "#,###,"),
                sumRowBottom = Template.HeadingFormat("Sum", formatData = "#,###,")

        ).build("src/eu/qiou/aaf4k/test/demo1.xls", "trail")
    }
}