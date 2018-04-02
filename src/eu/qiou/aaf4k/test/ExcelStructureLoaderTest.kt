package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.accounting.Balance
import eu.qiou.aaf4k.reportings.accounting.ProfitAndLoss
import eu.qiou.aaf4k.reportings.etl.ExcelDataLoader
import eu.qiou.aaf4k.reportings.etl.ExcelStructureLoader
import eu.qiou.aaf4k.util.TimeSpan
import org.junit.Test

import java.time.LocalDate

class ExcelStructureLoaderTest {

    @Test
    fun loadStructure() {
        val reporting =ExcelStructureLoader("src/eu/qiou/aaf4k/demo/DemoStructure.xls").loadStructure(Balance(2018001, "Demo_Balance", LocalDate.now()))
        val reporting1 =ExcelStructureLoader("src/eu/qiou/aaf4k/demo/DemoStructure.xlsx",1).loadStructure(ProfitAndLoss(2018001, "Demo_Profit & Loss", TimeSpan.forYear(2018)))

        reporting.loadData(dataLoader = ExcelDataLoader("src/eu/qiou/aaf4k/demo/data.xlsx"))
        reporting1.loadData(dataLoader = ExcelDataLoader("src/eu/qiou/aaf4k/demo/data.xlsx", 1))

        // println(reporting)
        // println(reporting1)
        println(reporting.toJSON())

    }
}