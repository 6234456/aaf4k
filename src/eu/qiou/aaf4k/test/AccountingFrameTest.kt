package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.etl.AccountingFrame
import eu.qiou.aaf4k.util.groupNearby
import org.junit.Test

class AccountingFrameTest {

    @Test
    fun getFlattened() {
        println(AccountingFrame.inflate(123, "cn_cas_2018"))
    }

    @Test
    fun groupNearBy() {
        println(listOf(1, 3, 3, 3, 4, 4, 5, 6, 1, 1, 3).groupNearby { i -> i })
        println(listOf(1, 3, 3, 3, 4, 4, 5, 6, 1, 1).groupNearby { i -> i * 0.1 })
    }
}