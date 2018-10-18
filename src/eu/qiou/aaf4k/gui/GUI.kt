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
                                    text = "报表"
                                    isClosable = false
                                    content = TreeTableView<Account>(root).apply {
                                        columns.addAll(
                                                cols
                                        )
                                        isFullScreen = true

                                        this.addEventHandler(MouseEvent.MOUSE_CLICKED) { e ->
                                            if (e.button == MouseButton.SECONDARY) {

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

                                                            val text = mutableListOf<AutoCompleteTextField<Int>>(
                                                                    AutoCompleteTextField<Int>(if (!targetAccount.hasChildren()) "${targetAccount.id} ${targetAccount.name}" else "", suggestions = suggestions).apply {
                                                                        if (!targetAccount.hasChildren())
                                                                            result = targetAccount.id
                                                                    },
                                                                    AutoCompleteTextField<Int>("", suggestions = suggestions).apply {

                                                                    }
                                                            )

                                                            val values = mutableListOf<NumericTextField>(
                                                                    NumericTextField(targetAccount.decimalPrecision),
                                                                    NumericTextField(targetAccount.decimalPrecision)
                                                            )


                                                            val rootPane = GridPane().apply {
                                                                hgap = 10.0
                                                                vgap = 10.0
                                                                text.forEachIndexed { i, e ->
                                                                    add(e, 0, i)
                                                                }
                                                                values.forEachIndexed { i, e ->
                                                                    add(e, 1, i)
                                                                }


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
                        )
                        tabs.add(
                                Tab().apply {
                                    text = "分录"
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