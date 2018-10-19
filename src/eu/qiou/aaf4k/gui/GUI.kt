package eu.qiou.aaf4k.gui

import eu.qiou.aaf4k.accounting.model.Account
import eu.qiou.aaf4k.accounting.model.Category
import eu.qiou.aaf4k.accounting.model.Entry
import eu.qiou.aaf4k.accounting.model.ReportingType
import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.test.AccountingFrameTest
import eu.qiou.aaf4k.util.roundUpTo
import javafx.application.Application
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.geometry.Side
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.stage.Stage

class GUI : Application() {

    companion object {
        fun open() {
            Application.launch(GUI::class.java)
        }
    }

    override fun start(primaryStage: Stage?) {

        val reporting = AccountingFrameTest.testReporting()

        val suggestions = reporting.flatten().map { "${it.id} ${it.name}" to it.id }.toMap()

        val colToCategory = reporting.categories.map { it.name to it }.toMap()

        val formatter: (Number, Int) -> String = { n, dec ->
            if (Math.abs(n.toDouble()) < Math.pow(10.0, -1.0 * (dec + 1))) "" else String.format("%,.${dec}f", n.roundUpTo(dec))
        }

        val root = TreeItem(
                Account.from(ProtoAccount(0, reporting.entity.name, 0L), ReportingType.AUTO)
        ).apply {
            isExpanded = true
        }

        fun inflateTreeItem(item: TreeItem<Account> = root, accounts: List<Account> = reporting.structure) {
            accounts.forEach {
                val parent = TreeItem(it)

                item.children.add(parent)
                item.isExpanded = true

                if (it.hasChildren()) {
                    inflateTreeItem(parent, it.subAccounts!!.toList() as List<Account>)
                }
            }
        }

        inflateTreeItem()

        val cols = listOf(
                TreeTableColumn<Account, String>("科目代码").apply {
                    setCellValueFactory {
                        ReadOnlyStringWrapper(it.value.value.id.toString())
                    }
                },
                TreeTableColumn<Account, String>("账户名称").apply {
                    setCellValueFactory {
                        ReadOnlyStringWrapper(
                                with(it.value.value) {
                                    if (isStatistical) "其中:$name" else name
                                }
                        )
                    }

                },
                TreeTableColumn<Account, String>("科目余额：调整前").apply {
                    setCellValueFactory {
                        ReadOnlyStringWrapper(formatter(it.value.value.displayValue, it.value.value.decimalPrecision))
                    }

                    style = "-fx-alignment:center-right"
                }
        ) +
                reporting.categories.map {
                    TreeTableColumn<Account, String>(it.name).apply {
                        val data = reporting.update(it.toDataMap()).flattenWithAllAccounts().map { it.id to it.displayValue }.toMap()
                        setCellValueFactory {
                            ReadOnlyStringWrapper(
                                    formatter(data.getOrDefault(it.value.value.id, 0.0), it.value.value.decimalPrecision)
                            )
                        }

                        style = "-fx-alignment:center-right"
                    }
                } +
                listOf(
                        TreeTableColumn<Account, String>("科目余额：调整后").apply {
                            val data = reporting.generate().flattenWithAllAccounts().map { it.id to it.displayValue }.toMap()
                            setCellValueFactory {
                                ReadOnlyStringWrapper(
                                        formatter(data.getOrDefault(it.value.value.id, 0.0), it.value.value.decimalPrecision)
                                )
                            }

                            style = "-fx-alignment:center-right"
                        }
                )

        cols.forEach {
            it.prefWidth = 200.0
            it.isSortable = false
        }

        with(primaryStage!!) {
            scene = Scene(
                    TabPane().apply {
                        side = Side.LEFT

                        tabs.add(
                                Tab().apply {
                                    text = "科目汇总"
                                    isClosable = false
                                    content = TreeTableView<Account>(root).apply {
                                        columns.addAll(
                                                cols
                                        )

                                        this.addEventHandler(MouseEvent.MOUSE_CLICKED) { e ->
                                            if (e.button == MouseButton.PRIMARY) {
                                                if (e.clickCount == 2) {
                                                    (e.target as TreeTableCell<Account, String>).let {

                                                        if (colToCategory.containsKey(it.tableColumnProperty().value.text)) {

                                                            val targetAccount = it.treeTableRow.treeItem.value
                                                            val category = colToCategory[it.tableColumnProperty().value.text]!! as Category

                                                            // booking mask
                                                            val dialog: Dialog<Entry> = Dialog()

                                                            dialog.run {
                                                                title = "Booking"
                                                                dialogPane.buttonTypes.addAll(
                                                                        ButtonType.OK,
                                                                        ButtonType.CANCEL
                                                                )

                                                                val rootPane = GridPane()

                                                                val text = mutableListOf<AutoCompleteTextField<Int>>(
                                                                        AutoCompleteTextField<Int>(if (!targetAccount.hasChildren()) "${targetAccount.id} ${targetAccount.name}" else "", suggestions = suggestions).apply {
                                                                            if (!targetAccount.hasChildren())
                                                                                result = targetAccount.id
                                                                        },
                                                                        AutoCompleteTextField<Int>("", suggestions = suggestions).apply {

                                                                        }
                                                                )

                                                                val values = text.map {
                                                                        NumericTextField(targetAccount.decimalPrecision)
                                                                }.toMutableList()


                                                                val btnBalance = text.mapIndexed { i, _ ->
                                                                    Button("b").apply {
                                                                        setOnAction {
                                                                            values[i].writeNumber(values.foldIndexed(0.0) { index, acc, e ->
                                                                                acc + if (e.number == null || index == i) 0.0 else e.number!!.toDouble()
                                                                            } * -1)
                                                                        }
                                                                    }
                                                                }.toMutableList()

                                                                val btnPlus = text.map {
                                                                    Button("+")
                                                                }.toMutableList()

                                                                val btnMinus = text.map {
                                                                    Button("-")
                                                                }.toMutableList()

                                                                val elements = listOf(
                                                                        text,
                                                                        values,
                                                                        btnBalance,
                                                                        btnPlus,
                                                                        btnMinus)

                                                                val group = ControlGroup(elements, listOf(
                                                                        { i: Int, g: ControlGroup ->
                                                                            AutoCompleteTextField<Int>("", suggestions = suggestions)
                                                                        },
                                                                        { i: Int, g: ControlGroup ->
                                                                            NumericTextField(targetAccount.decimalPrecision)
                                                                        },
                                                                        { i: Int, g: ControlGroup ->
                                                                            Button("b").apply {
                                                                                setOnAction {
                                                                                    val j = elements[i].indexOf(this)
                                                                                    values[i].writeNumber(values.foldIndexed(0.0) { index, acc, e ->
                                                                                        acc + if (e.number == null || index == j) 0.0 else e.number!!.toDouble()
                                                                                    } * -1)
                                                                                }
                                                                            }
                                                                        },
                                                                        { i: Int, g: ControlGroup ->
                                                                            Button("+").apply {
                                                                                setOnAction { g.append(elements[i].indexOf(this), rootPane) }
                                                                            }
                                                                        },
                                                                        { i: Int, g: ControlGroup ->
                                                                            Button("-").apply {
                                                                                setOnAction { g.remove(elements[i].indexOf(this), rootPane) }
                                                                            }
                                                                        }
                                                                ))

                                                                btnPlus.forEachIndexed { index, button ->
                                                                    button.apply {
                                                                        setOnAction {
                                                                            group.append(elements[index].indexOf(this), rootPane)
                                                                        }
                                                                    }
                                                                }
                                                                btnMinus.forEachIndexed { index, button ->
                                                                    button.apply {
                                                                        setOnAction {
                                                                            group.remove(elements[index].indexOf(this), rootPane)
                                                                        }
                                                                    }
                                                                }


                                                                rootPane.apply {
                                                                    hgap = 10.0
                                                                    vgap = 10.0

                                                                    prefHeight = 600.0

                                                                    group.attachToRoot(this)
                                                                }


                                                                dialogPane.content = rootPane

                                                                setResultConverter {
                                                                    if (it == ButtonType.OK) {
                                                                        with(text.map { it.result }.zip(values.map { it.number })) {
                                                                            if (this.any { it.first != null && it.second != null }) {
                                                                                Entry(category.nextEntryIndex, "", category).apply {
                                                                                    this@with.forEach { p ->
                                                                                        p.first?.let { id ->
                                                                                            p.second?.let {
                                                                                                this.add(id, it.toDouble())
                                                                                            }
                                                                                        }

                                                                                    }
                                                                                }
                                                                            } else {
                                                                                null
                                                                            }
                                                                        }
                                                                    } else {
                                                                        null
                                                                    }
                                                                }

                                                                showAndWait().ifPresent {
                                                                    println(it)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                        )
                        tabs.add(
                                Tab().apply {
                                    text = "财务报表"
                                    isClosable = false
                                }
                        )
                        tabs.add(
                                Tab().apply {
                                    text = "调整分录"
                                    isClosable = false
                                }
                        )
                    }


            )

            title = "Reporting"
            show()
        }

    }
}