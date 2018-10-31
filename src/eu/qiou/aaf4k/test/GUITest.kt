package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.gui.GUI
import org.junit.Test

class GUITest {

    @Test
    fun open() {
        //GUI.open(Files.readAllLines(Paths.get("data/accounting.txt")).joinToString("\n").toReporting())
        GUI.open(AccountingFrameTest.testReporting())
    }
}