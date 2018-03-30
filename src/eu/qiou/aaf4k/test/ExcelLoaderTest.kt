package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.accounting.Balance
import eu.qiou.aaf4k.reportings.accounting.ProfitAndLoss
import eu.qiou.aaf4k.reportings.etl.ExcelLoader
import eu.qiou.aaf4k.util.TimeSpan
import org.junit.Test

import java.time.LocalDate

class ExcelLoaderTest {

    @Test
    fun loadStructure() {
        val reporting =ExcelLoader("src/eu/qiou/aaf4k/demo/DemoStructure.xlsx").loadStructure(Balance(2018001, "Demo_Balance", LocalDate.now()))
        println(reporting)

        val reporting1 =ExcelLoader("src/eu/qiou/aaf4k/demo/DemoStructure.xls",1).loadStructure(ProfitAndLoss(2018001, "Demo_Balance", TimeSpan.forYear(2018)))
        println(reporting1)
    }
}