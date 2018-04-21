package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.etl.ExcelDataLoader
import eu.qiou.aaf4k.reportings.etl.ExcelStructureLoader
import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.reportings.model.ProtoCategory
import eu.qiou.aaf4k.reportings.model.ProtoEntry
import eu.qiou.aaf4k.reportings.model.ProtoReporting
import org.junit.Test

class ProtoReportingTest {

    @Test
    fun update() {
        val structure = ExcelStructureLoader("src/eu/qiou/aaf4k/demo/DemoStructure.xlsx", sheetName = "Bilanz").load()

        val data = ExcelDataLoader("src/eu/qiou/aaf4k/demo/data.xlsx", sheetName = "Bilanz").load()

        val reporting = ProtoReporting(0, "Demo", structure = structure).update(data)

        val c1 = ProtoCategory("KapKons", 1, "Kapitalkonsolidierung", reporting)
        val c2 = ProtoCategory("SchuldenKons", 2, "Schukonsolidierung", reporting)

        val d1 = ProtoEntry(123, "konsbu1", c1)
        val d2 = ProtoEntry(1234, "konsbu2", c1)

        val a1 = ProtoAccount(200, "acc1", -10000L)
        val a2 = ProtoAccount(400, "acc1", 10000L)

        d1.add(a1)
        d1.add(a2)

        println(d1)

        println(reporting.generate())

    }
}

