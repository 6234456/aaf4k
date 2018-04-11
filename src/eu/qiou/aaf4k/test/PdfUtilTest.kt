package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.PdfUtil
import org.junit.Test
import java.awt.Rectangle

class PdfUtilTest {

    @Test
    fun extractText() {
        val a1 = PdfUtil.extractText(
                    PdfUtil.readFile("src/eu/qiou/aaf4k/test/credentials.SKR 04 2017.pdf"),
                    pageFilter = {i, _ -> i == 1},
                    regions = mutableMapOf(
                        "a" to Rectangle(150,150,400,400)
                    )
        )

        println(a1)
    }
}