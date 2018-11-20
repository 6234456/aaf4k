package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.accounting.model.Category
import eu.qiou.aaf4k.gui.GUI
import eu.qiou.aaf4k.gui.StringParser
import eu.qiou.aaf4k.reportings.etl.AccountingFrame
import org.junit.Test
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
        val e = AccountingFrame.inflate(123, "SKR4-2018", "data/de/credentials.de_hgb_2018.txt")

        Category("SchKons", 1, "Schuldenkonsolidierung", e)
        Category("KapKons", 2, "Kapitalkonsolidierung", e)
        Category("A/E-Kons", 3, "Aufwands-und Ertragskonsolidierung", e)

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
    fun parseBinding() {
        val l = mutableListOf<Double>()
        val v = StringParser.parseBindingString("=$1+$2+0.345 ", Double::toDouble, l)
        l.add(1.0)
        l.add(1.0)
        l.add(1.0)
        l.add(1.0)
        println(v())
    }
}