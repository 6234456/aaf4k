package eu.qiou.aaf4k.gui

import javafx.scene.control.Control
import javafx.scene.layout.GridPane


// Int : the index of the Control type, ControlGroup this
class ControlGroup(private val methodAdd: List<(Int, ControlGroup) -> Control>, private val startCol: Int = 0, private val startRow: Int = 0) {


    val elements = 0.until(methodAdd.size).map {
        mutableListOf(methodAdd[it](it, this))
    }

    fun remove(i: Int, root: GridPane? = null) {
        if (length > 2) {

            elements.forEach {
                val e = it.removeAt(i)
                root?.children?.remove(e)
            }
        }
    }

    fun append(index: Int, root: GridPane? = null, startCol: Int = this.startCol, startRow: Int = this.startRow) {
        val l = length - 1
        elements.forEachIndexed { i, mutableList ->
            val e = methodAdd[i](i, this)

            root?.let {
                l.downTo(index).forEach { k ->
                    if (k >= index) {
                        GridPane.setRowIndex(mutableList[k], k + startRow + 1)
                    }
                }
                it.add(e, i + startCol, index + startRow)
            }
            mutableList.add(index, e)
        }
    }

    fun attachToRoot(root: GridPane, startCol: Int = this.startCol, startRow: Int = this.startRow) {
        elements.forEachIndexed { i, list ->
            list.forEachIndexed { j, node ->
                root.add(node, i + startCol, j + startRow)
            }
        }
    }

    fun inflate(n: Int) {
        (n - 1).downTo(1).forEach { _ ->
            append(length - 1)
        }
    }

    private val length: Int
        get() = elements[0].size

}