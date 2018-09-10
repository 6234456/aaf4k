package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.SZSEDiscloure
import org.junit.Test

class SZSEDiscloureTest {

    @Test
    fun get() {
        // 000612
        // http://disc.static.szse.cn/download
        // SZSEDiscloure.getPdfLinks("000612")
        println(SZSEDiscloure.getEntityInfoById("04"))
    }
}