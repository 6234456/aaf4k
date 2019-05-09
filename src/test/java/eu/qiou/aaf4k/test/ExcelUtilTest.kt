package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.template.Template
import org.apache.poi.ss.usermodel.IndexedColors
import org.junit.Test
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*


class ExcelUtilTest {

    @Test
    fun writeData() {
        ExcelUtil.writeData("src/eu/qiou/aaf4k/test/demo1.xls", data = mapOf("1" to listOf(23.4, LocalDate.now()), "2" to listOf(234.123123)))
    }

    @Test
    fun formatStrTest() {
        println(ExcelUtil.formatSpecifier.matchEntire(",00#.#0#")!!.groups)
        println(ExcelUtil.formatSpecifier.matchEntire("#,00#.#0#")!!.groups)
        println(ExcelUtil.formatSpecifier.matchEntire("#")!!.groups)
        println(ExcelUtil.formatSpecifier.matchEntire("#,00#.")!!.groups)
        println(ExcelUtil.formatSpecifier.matchEntire("##,000,#00,00#.00##00")!!.groups)
        println(ExcelUtil.formatSpecifier.matchEntire(",,,00#.")!!.groups)
        println(ExcelUtil.formatSpecifier.find("######.")!!.groups)
        println(ExcelUtil.formatSpecifier.find("###0#")!!.groups)

        println(String.format("%,.0f", 123456.0))

        println(ExcelUtil.parseXlFormatString("#")(11123))
        println(ExcelUtil.parseXlFormatString("#.")(11123))
        println(ExcelUtil.parseXlFormatString("#,###.")(11123))
        println(ExcelUtil.parseXlFormatString("#,###.000")(11123))

    }

    @Test
    fun rgb() {
        println(ExcelUtil.longToRGB(11892015L))
    }

    @Test
    fun fmtStr() {
        val f = (NumberFormat.getInstance(Locale.ENGLISH) as DecimalFormat)

        val s = "#"
        val n = -1123.34
        f.applyPattern(s)
        println(f.format(n))
        println(ExcelUtil.parseXlFormatString(s)(n))
    }

    @Test
    fun formatExcel() {
        Template(
                listOf(
                        Template.HeadingFormat("杨  ", ExcelUtil.DataFormat.STRING.format, ExcelUtil.DataFormat.STRING.format),
                        Template.HeadingFormat(LocalDate.now(), "mmm yyyy", "#,###,")
                ),
                listOf(
                        mapOf("0" to "Hello", "1" to -12345),
                        mapOf("0" to "Hello7", "1" to 123415)
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