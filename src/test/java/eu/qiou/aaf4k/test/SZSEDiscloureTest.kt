package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.SZSEDiscloure
import eu.qiou.aaf4k.util.io.PdfUtil
import org.junit.Test
import java.awt.Rectangle
import java.net.URL

class SZSEDiscloureTest {

    @Test
    fun get() {
        // 000612
        // http://disc.static.szse.cn/download
        // SZSEDiscloure.getPdfLinks("000612")
        println(SZSEDiscloure.getEntityInfoById("04"))
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
}