package eu.qiou.aaf4k.gui

import javafx.scene.control.Control
import javafx.scene.layout.GridPane


// Int : the index of the Control type, ControlGroup this
class ControlGroup(val methodAdd: List<(Int, ControlGroup) -> Control>) {

    val elements = 0.until(methodAdd.size).map {
        mutableListOf(methodAdd[it](it, this))
    }

    fun remove(i: Int, root: GridPane? = null) {
        if (length > 2) {

            elements.forEach {
                val e = it.removeAt(i)
                root?.let {
                    it.children.remove(e)
                }
            }
        }
    }

    fun getControlType(index: Int): List<Control> {
        return elements[index]
    }


    fun append(index: Int, root: GridPane? = null, startCol: Int = 0, startRow: Int = 0) {
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

    fun attachToRoot(root: GridPane, startCol: Int = 0, startRow: Int = 0) {
        elements.forEachIndexed { i, list ->
            list.forEachIndexed { j, node ->
                root.add(node, i + startCol, j + startRow)
            }
        }
    }

    fun inflate(n: Int) {
        (n - 1).downTo(1).forEach {
            append(length - 1)
        }
    }

    var length: Int = elements[0].size
        get() = elements[0].size
        private set

}