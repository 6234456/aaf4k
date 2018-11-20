package eu.qiou.aaf4k.gui

import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.strings.times
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import org.apache.poi.ss.usermodel.*

class XlTable(xlSht: Sheet, hasHeading: Boolean = true) : TableView<Map<String, Cell>>() {
    private val firstRow = ExcelUtil.getFirstNonEmptyRowNum(xlSht)
    private val heading = hasHeading && firstRow == 0
    private val data = FXCollections.observableArrayList<Map<String, Cell>>()
    private val title = if (heading)
        xlSht.getRow(0).cellIterator().asSequence().map { it.columnIndex to ExcelUtil.textValue(it) }.toMap()
    else
        0.until(ExcelUtil.getColNum(xlSht) + 1).map { it to "Col-$it" }.toMap()

    private val columns = title.map {
        val k = it.value
        TableColumn<Map<String, Cell>, String>(it.value).apply {

            isSortable = false
            isResizable = true

            setCellValueFactory {
                it.value[k]?.let {
                    return@setCellValueFactory ReadOnlyStringWrapper(
                            "  " * it.cellStyle.indention.toInt() + ExcelUtil.textValue(it)
                    )
                }

                return@setCellValueFactory ReadOnlyStringWrapper("")
            }

            setCellFactory {
                object : TableCell<Map<String, Cell>, String>() {
                    override fun updateItem(item: String?, empty: Boolean) {
                        super.updateItem(item, empty)

                        if (item == null || empty) {
                            text = null
                            graphic = null
                        } else {
                            text = item
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


                                    if (it.cellStyle.borderBottomEnum != BorderStyle.NONE) {
                                        styleClass.add("btm-border")
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    init {
        (if (heading) 1 else 0).until(xlSht.lastRowNum + 1)
                .forEach {
                    val d = mutableMapOf<String, Cell>()
                    xlSht.getRow(it)?.cellIterator()?.forEach {
                        val c = it.columnIndex
                        d.put(title[c]!!, it)
                    }
                    data.add(d)
        }

        items = data
        getColumns().setAll(columns)
        if (!heading) styleClass.add("hide-header")
    }
}