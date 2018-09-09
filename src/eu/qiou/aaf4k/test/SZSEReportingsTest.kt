package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.SZSEReportings
import org.junit.Test

class SZSEReportingsTest {

    @Test
    fun get() {
        // 000612
        // http://disc.static.szse.cn/download
        // SZSEReportings.getPdfLinks("000612")
        println(SZSEReportings.getEntityInfoById("006"))
    }
}