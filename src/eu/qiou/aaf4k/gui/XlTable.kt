package eu.qiou.aaf4k.gui

import eu.qiou.aaf4k.util.io.ExcelUtil
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.MapValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.Callback
import javafx.util.StringConverter
import org.apache.poi.ss.usermodel.Sheet

class XlTable(xlSht: Sheet) : TableView<Map<String, String>>() {
    private val data = FXCollections.observableArrayList<Map<String, String>>()
    private val title = xlSht.getRow(0).cellIterator().asSequence().map { it.columnIndex to ExcelUtil.textValue(it) }.toMap()
    private val columns = title.map {
        TableColumn<Map<String, String>, String>(it.value).apply {
            this.cellValueFactory = MapValueFactory<String>(it.value) as Callback<TableColumn.CellDataFeatures<Map<String, String>, String>, ObservableValue<String>>

            this.setCellFactory {
                TextFieldTableCell(object : StringConverter<String>() {
                    override fun toString(`object`: String?): String {
                        return `object` ?: ""
                    }

                    override fun fromString(string: String?): String {
                        return string ?: ""
                    }
                })
            }
        }
    }

    init {

        xlSht.rowIterator().forEach {
            val r = it.rowNum
            if (r > 0) {
                val d = mutableMapOf<String, String>()
                it.cellIterator().forEach {
                    val c = it.columnIndex
                    d.put(title[c]!!, ExcelUtil.textValue(it))
                }
                data.add(d)
            }
        }

        this.items = data
        this.getColumns().setAll(columns)

    }
}