package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.CNInfoDiscloure
import org.junit.Test

class CNInfoDiscloureTest {
    val code = "szmb000035"

    @Test
    fun management() {
        println(CNInfoDiscloure.management(code))
    }

    @Test
    fun general() {
        println(CNInfoDiscloure.generalInfo(code))
    }

    @Test
    fun fs() {
        println(CNInfoDiscloure.fs(code, 2018, 2, CNInfoDiscloure.FSType.BALANCE_STMT))
        println(CNInfoDiscloure.fs(code, 2018, 2, CNInfoDiscloure.FSType.INCOME_STMT))
        println(CNInfoDiscloure.fs(code, 2018, 2, CNInfoDiscloure.FSType.CASHFLOW_STMT))
    }


}