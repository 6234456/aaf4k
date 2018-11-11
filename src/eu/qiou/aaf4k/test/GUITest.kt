package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.toReporting
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class GUITest {

    @Test
    fun open() {
        //GUI.open(Files.readAllLines(Paths.get("data/de_accounting.txt")).joinToString("\n").toReporting())
        //GUI.open(Files.readAllLines(Paths.get("data/accounting.txt")).joinToString("\n").toReporting())
        //GUI.open(AccountingFrameTest.testReporting())

        val e = Files.readAllLines(Paths.get("data/accounting.txt")).joinToString("\n").toReporting()
        println(e)
        println(e.checkDuplicate())
    }
}