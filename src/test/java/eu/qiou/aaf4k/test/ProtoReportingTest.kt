package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.gui.GUI
import eu.qiou.aaf4k.reportings.base.*
import eu.qiou.aaf4k.reportings.etl.ExcelDataLoader
import eu.qiou.aaf4k.reportings.etl.ExcelStructureLoader
import org.junit.Test
import java.util.*

class ProtoReportingTest {

    @Test
    fun update() {
        val structure = ExcelStructureLoader("src/eu/qiou/aaf4k/demo/DemoStructure.xlsx", sheetName = "Bilanz").load()

        val data = ExcelDataLoader("src/eu/qiou/aaf4k/demo/data.xlsx", sheetName = "Bilanz").load()

        val reporting = Reporting(CollectionAccount(0, "Demo").apply {
            addAll(structure)
        }).apply {
            update(data)
        }


        println(reporting.toJSON())

        val c1 = Category("KapKons",  "Kapitalkonsolidierung", reporting)
        val c2 = Category("SchuldenKons", "Schukonsolidierung", reporting)

        val d1 = Entry( "konsbu1", c1)
        val d2 = Entry("konsbu2", c1)

        val a1 = Account(200, "acc1", -10000L)
        val a2 = Account(400, "acc1", 10000L)

        d1.add(a1)
        d1.add(a2)

        println(d1)

        println(reporting.generate())

    }

    @Test
    fun reportings() {
        val l = Locale.GERMAN
        GUI.locale = l
        GUI.open(AccountingFrame.inflate(2018, "hgb", "data/de/credentials.de_hgb_2018.txt").toReporting().apply {
            Category("123", "123", this)
            prepareConsolidation(l)
        })
    }
}

