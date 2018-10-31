package eu.qiou.aaf4k.gui

import eu.qiou.aaf4k.util.io.ExcelUtil
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import org.apache.poi.ss.usermodel.Sheet

class XlPane(xlSht: Sheet) : GridPane() {
    init {
        xlSht.rowIterator().forEach {
            val r = it.rowNum
            it.cellIterator().forEach {
                val c = it.columnIndex
                this.add(Label(ExcelUtil.textValue(it)), c, r)
            }
        }
    }
}