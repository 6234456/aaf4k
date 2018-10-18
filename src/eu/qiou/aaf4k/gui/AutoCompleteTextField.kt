package eu.qiou.aaf4k.gui

import javafx.geometry.Side
import javafx.scene.control.ContextMenu
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.Label
import javafx.scene.control.TextField

class AutoCompleteTextField<T>(text: String = "", val suggestions: Map<String, T>) : TextField(text) {

    var result: T? = null
    private val dropDownList: ContextMenu = ContextMenu()

    init {
        textProperty().addListener { _, _, newValue ->
            newValue.let { t ->
                if (t.isBlank()) {
                    dropDownList.hide()
                } else {
                    with(suggestions.filter { it.key.contains(t, true) }) {
                        if (!this.isEmpty()) {
                            dropDownList.items.clear()
                            dropDownList.items.addAll(
                                    this.toList().take(20).toMap().map { s ->
                                        CustomMenuItem(Label().apply {
                                            this.text = s.key
                                            this.prefHeight = 20.0
                                        }).apply {
                                            setOnAction {
                                                this@AutoCompleteTextField.text = s.key
                                                this@AutoCompleteTextField.result = s.value
                                                dropDownList.hide()
                                            }
                                        }
                                    }
                            )


                            if (!dropDownList.isShowing)
                                dropDownList.show(this@AutoCompleteTextField, Side.BOTTOM, 0.0, 0.0)
                        } else {
                            dropDownList.hide()
                        }
                    }
                }
            }
        }

        focusedProperty().addListener { _, _, _ ->
            dropDownList.hide()
        }
    }
}