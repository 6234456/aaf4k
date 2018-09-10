package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.CNInfoDiscloure
import org.junit.Test

class CNInfoDiscloureTest {

    @Test
    fun management() {
        println(CNInfoDiscloure.management("szmb000006"))
    }

    @Test
    fun general() {
        println(CNInfoDiscloure.generalInfo("szmb000006"))
    }

    @Test
    fun fs() {
        println(CNInfoDiscloure.fs("000006", 2017, 2, CNInfoDiscloure.FSType.BALANCE_STMT))
        println(CNInfoDiscloure.fs("000006", 2017, 2, CNInfoDiscloure.FSType.INCOME_STMT))
    }


}