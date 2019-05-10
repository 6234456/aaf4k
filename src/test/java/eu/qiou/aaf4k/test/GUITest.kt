package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.gui.GUI
import eu.qiou.aaf4k.gui.StringParser
import eu.qiou.aaf4k.reportings.base.AccountingFrame
import eu.qiou.aaf4k.reportings.base.Category
import eu.qiou.aaf4k.util.io.toReporting
import eu.qiou.aaf4k.util.time.TimeParameters
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class GUITest {

    @Test
    fun open() {
        //GUI.open(Files.readAllLines(Paths.get("data/de_accounting.txt")).joinToString("\n").toReporting())
        //GUI.open(Files.readAllLines(Paths.get("data/accounting.txt")).joinToString("\n").toReporting())
        //GUI.open(AccountingFrameTest.testReporting())

        //val e = Files.readAllLines(Paths.get("data/accounting.txt")).joinToString("\n").toReporting()
        //println(e)
        //println(e.checkDuplicate())
        val e = AccountingFrame.inflate(123, "SKR4-2018", "data/de/credentials.de_hgb_2018.txt").toReporting()

        Category("SchKons", "Schuldenkonsolidierung", e)
        Category("KapKons", "Kapitalkonsolidierung", e)
        Category("A/E-Kons", "Aufwands-und Ertragskonsolidierung", e)

        GUI.srcJSONFile = "data/de_accounting.txt"
        GUI.open(e)
    }

    @Test
    fun open2() {
        GUI.locale = Locale.GERMAN
        GUI.open("data/de_accounting.txt")
        //GUI.open(AccountingFrameTest.testReporting())

        //val e = Files.readAllLines(Paths.get("data/de_accounting.txt")).joinToString("\n").toReporting()
        //println(e.categories.find { it.name == "SchKons" }!!.nextEntryIndex)
        //println(e.checkDuplicate())
    }

    @Test
    fun guess() {
        val e = Files.readAllLines(Paths.get("data/de_accounting.txt")).joinToString("\n").toReporting()
        //println(e.categories.find { it.name == "SchKons" }!!.nextEntryIndex)
        println(e.guessSuperAccount(4641))
        val f = e.carryForward().shorten()
        f.toXl("data/exp.xls")
    }

    @Test
    fun parseBinding() {
        val l = mutableListOf<Double>()
        val v = StringParser.parseBindingString("=$1+$2+0.345 ", Double::toDouble, l)
        l.add(1.0)
        l.add(1.0)
        l.add(1.0)
        l.add(1.0)
        println(v())
    }

    @Test
    fun open3() {
        val f = AccountingFrame.inflate(123, "cn_cas1_2018")
        GUI.locale = Locale.ENGLISH
        GUI.open(f.toReporting(123, "Demo2", TimeParameters.forYear(2018)).apply {
            prepareConsolidation(Locale.CHINESE)
            println(this.categories.map {  "${it.id} " + it.name})
        })
    }

    @Test
    fun open4() {
        val f = AccountingFrame.inflate(123, "de_hgb_2018")
        GUI.locale = Locale.GERMAN
        GUI.open(f.toReporting(123, "Demo2", TimeParameters.forYear(2018)).apply {
            prepareConsolidation(Locale.GERMAN)
        })
    }
}