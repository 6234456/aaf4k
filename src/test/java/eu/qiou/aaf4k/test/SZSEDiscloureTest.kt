package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.SZSEDiscloure
import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.io.PdfUtil
import eu.qiou.aaf4k.util.template.Template
import org.junit.Test
import java.awt.Rectangle
import java.net.URL

class SZSEDiscloureTest {

    @Test
    fun get() {
        // 000612
        // http://disc.static.szse.cn/download
        // SZSEDiscloure.getPdfLinks("000612")

        val d = SZSEDiscloure.getEntityInfoById("600", 2000).filter { it.value.auditor.contains("立信") }.map {
            mapOf(
                    "id" to it.key,
                    "name" to it.value.orgName,
                    "nameEN" to it.value.orgNameEN,
                    "pdf" to SZSEDiscloure.getPdfLinks(it.value.SECCode, 2018, 4, true)
            )
        }

        Template(listOf(
                Template.HeadingFormat(value = "id", formatData = ExcelUtil.DataFormat.STRING.format),
                Template.HeadingFormat(value = "name", formatData = ExcelUtil.DataFormat.STRING.format, columnWidth = 48),
                Template.HeadingFormat(value = "nameEN", formatData = ExcelUtil.DataFormat.STRING.format),
                Template.HeadingFormat(value = "pdf", formatData = ExcelUtil.DataFormat.STRING.format)
        ), d).build("data/trail10.xlsx")


    }
    @Test
    fun getPdf() {
        // 000612
        // http://disc.static.szse.cn/download
        // SZSEDiscloure.getPdfLinks("000612")

        println(
                PdfUtil.extractText(
                        PdfUtil.readFile(URL(SZSEDiscloure.getPdfLinks(12, 2017, 4).values.first()))
                        , regions =
                mapOf(
                        "a" to Rectangle(50, 50, 690, 750)
                )
                )[8])
    }

    @Test
    fun trail2() {
        println(SZSEDiscloure.get(10).map { it.value })
    }
}