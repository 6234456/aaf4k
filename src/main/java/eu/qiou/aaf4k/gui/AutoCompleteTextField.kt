package eu.qiou.aaf4k.gui

import javafx.geometry.Side
import javafx.scene.control.ContextMenu
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

class AutoCompleteTextField<T>(text: String = "", val suggestions: Map<String, T>) : TextField(text) {

    var result: T? = null
    private var programOverride = false
    private val dropDownList: ContextMenu = ContextMenu()

    fun setTextValue(s: String) {
        programOverride = true
        text = s
        programOverride = false
    }

    init {
        textProperty().addListener { _, _, newValue ->
            newValue.let { t ->
                if (t.isBlank()) {
                    dropDownList.hide()
                } else {
                    with(suggestions.filter { it.key.contains(t, true) }) {
                        if (!this.isEmpty()) {
                            val l = this.size
                            dropDownList.items.clear()
                            dropDownList.items.addAll(
                                    this.toList().take(20).toMap().map { s ->
                                        CustomMenuItem(Label().apply {
                                            this.text = s.key
                                            this.prefHeight = 20.0

                                            addEventFilter(KeyEvent.KEY_RELEASED) {
                                                if (it.code == KeyCode.TAB) {
                                                    //TODO TAB - EVENT
                                                    println("pressed")
                                                }
                                            }

                                        }).apply {
                                            this.userData = s.key to s.value
                                            setOnAction {
                                                this@AutoCompleteTextField.text = s.key
                                                this@AutoCompleteTextField.result = s.value
                                                dropDownList.hide()
                                            }
                                        }
                                    }
                            )


                            if (!dropDownList.isShowing && !programOverride)
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

        this.addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.DOWN) {
                if (dropDownList.isShowing) {
                    if (dropDownList.items.count() == 1) {
                        val res = dropDownList.items[0].userData as Pair<String, T>
                        this.text = res.first
                        this.result = res.second
                        dropDownList.hide()
                    } else {
                        dropDownList.skin.node.lookup(".menu-item").requestFocus()
                    }
                }
            }
        }
    }
}