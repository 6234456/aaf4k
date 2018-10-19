package eu.qiou.aaf4k.gui

import javafx.scene.control.Control
import javafx.scene.layout.GridPane


// Int : the index of the Control type, ControlGroup this
class ControlGroup(val elements: List<MutableList<out Control>>,
                   val methodAdd: List<(Int, ControlGroup) -> Control>,
                   val rows: Int = 3
) {

    val n = methodAdd.size
    val ele = 0.until(n).map {
        mutableListOf(methodAdd[it](it, this))
    }


    fun remove(i: Int, root: GridPane? = null) {
        println(length)
        if (length > 2)
            elements.forEach {
                val e = it.removeAt(i)
                root?.let {
                    it.children.remove(e)
                }
            }
    }

    fun append(index: Int, root: GridPane? = null, startCol: Int = 0, startRow: Int = 0) {
        println("index: $index:  length: $length")
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
            (mutableList as MutableList<Control>).add(index, e)
        }
    }

    fun attachToRoot(root: GridPane, startCol: Int = 0, startRow: Int = 0) {
        elements.forEachIndexed { i, list ->
            list.forEachIndexed { j, node ->
                root.add(node, i + startCol, j + startRow)
            }
        }
    }

    fun inflate() {

    }

    var length: Int = elements[0].size
        get() = elements[0].size
        private set

}