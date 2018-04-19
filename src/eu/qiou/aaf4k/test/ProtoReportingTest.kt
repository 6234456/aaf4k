package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.etl.ExcelDataLoader
import eu.qiou.aaf4k.reportings.etl.ExcelStructureLoader
import eu.qiou.aaf4k.reportings.model.ProtoReporting
import org.junit.Test

class ProtoReportingTest {

    @Test
    fun update() {
        val structure = ExcelStructureLoader("src/eu/qiou/aaf4k/demo/DemoStructure.xlsx", sheetName = "Bilanz").load()
        val reporting = ProtoReporting(0, "Demo", structure = structure)

        val data = ExcelDataLoader("src/eu/qiou/aaf4k/demo/data.xlsx", sheetName = "Bilanz").load()

        println(reporting.update(data).structure.map { "${it.name} ${it.textValue}" })
    }
}

