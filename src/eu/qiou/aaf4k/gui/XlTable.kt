package eu.qiou.aaf4k.gui

import eu.qiou.aaf4k.util.io.ExcelUtil
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.Sheet

class XlTable(xlSht: Sheet) : TableView<Map<String, Cell>>() {
    private val data = FXCollections.observableArrayList<Map<String, Cell>>()
    private val title = xlSht.getRow(0).cellIterator().asSequence().map { it.columnIndex to ExcelUtil.textValue(it) }.toMap()
    private val columns = title.map {
        val k = it.value
        TableColumn<Map<String, Cell>, String>(it.value).apply {

            setCellValueFactory {
                it.value[k]?.let {
                    return@setCellValueFactory ReadOnlyStringWrapper(ExcelUtil.textValue(it))
                }

                return@setCellValueFactory ReadOnlyStringWrapper("")
            }

            setCellFactory {
                object : TableCell<Map<String, Cell>, String>() {
                    override fun updateItem(item: String?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = if (empty) "" else item ?: ""
                        val i = this.index
                        if (i >= 0 && i < data.size) {
                            data[i][k]?.let {
                                when (it.cellTypeEnum) {
                                    CellType.NUMERIC, CellType.FORMULA -> this.alignment = Pos.CENTER_RIGHT
                                    else -> this.alignment = Pos.CENTER_LEFT
                                }


                                when (it.cellStyle.alignmentEnum) {
                                    HorizontalAlignment.RIGHT -> this.alignment = Pos.CENTER_RIGHT
                                    HorizontalAlignment.LEFT -> this.alignment = Pos.CENTER_LEFT
                                    HorizontalAlignment.CENTER -> this.alignment = Pos.CENTER
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        xlSht.rowIterator().forEach {
            val r = it.rowNum
            if (r > 0) {
                val d = mutableMapOf<String, Cell>()
                it.cellIterator().forEach {
                    val c = it.columnIndex
                    d.put(title[c]!!, it)
                }
                data.add(d)
            }
        }

        this.items = data
        this.getColumns().setAll(columns)

    }
}