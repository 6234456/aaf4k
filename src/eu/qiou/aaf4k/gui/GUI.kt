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
import javafx.collections.FXCollections
import javafx.geometry.Side
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import javafx.util.StringConverter

class GUI : Application() {

    companion object {
        fun open() {
            Application.launch(GUI::class.java)
        }
    }

    override fun start(primaryStage: Stage?) {

        val reporting = AccountingFrameTest.testReporting()

        val suggestions = reporting.flatten().map { "${it.id} ${it.name}" to it.id }.toMap()

        val categories = FXCollections.observableArrayList<Category>().apply {
            reporting.categories.map {
                this.add(it as Category)
            }
        }

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
                                                            var category = colToCategory[it.tableColumnProperty().value.text]!! as Category

                                                            // booking mask
                                                            val dialog: Dialog<Entry> = Dialog()

                                                            dialog.run {
                                                                title = "Booking"
                                                                dialogPane.buttonTypes.addAll(
                                                                        ButtonType.OK,
                                                                        ButtonType.CANCEL
                                                                )

                                                                val rootPane = GridPane()

                                                                val valuePos = 1

                                                                val group = ControlGroup(listOf(
                                                                        { i: Int, g: ControlGroup ->
                                                                            AutoCompleteTextField<Int>("", suggestions = suggestions)
                                                                        },
                                                                        { i: Int, g: ControlGroup ->
                                                                            NumericTextField(targetAccount.decimalPrecision)
                                                                        },
                                                                        { i: Int, g: ControlGroup ->
                                                                            Button("b").apply {
                                                                                setOnAction {
                                                                                    val j = g.elements[i].indexOf(this)
                                                                                    val values = g.elements[valuePos] as MutableList<NumericTextField>
                                                                                    values[j].writeNumber(values.foldIndexed(0.0) { index, acc, e ->
                                                                                        acc + if (e.number == null || index == j) 0.0 else e.number!!.toDouble()
                                                                                    } * -1)
                                                                                }
                                                                            }
                                                                        },
                                                                        { i: Int, g: ControlGroup ->
                                                                            Button("+").apply {
                                                                                setOnAction { g.append(g.elements[i].indexOf(this), rootPane) }
                                                                            }
                                                                        },
                                                                        { i: Int, g: ControlGroup ->
                                                                            Button("-").apply {
                                                                                setOnAction { g.remove(g.elements[i].indexOf(this), rootPane) }
                                                                            }
                                                                        }
                                                                ), 0, 1).apply {
                                                                    inflate(3)

                                                                    this.elements[0][0] = AutoCompleteTextField<Int>(if (!targetAccount.hasChildren()) "${targetAccount.id} ${targetAccount.name}" else "", suggestions).apply {
                                                                        if (!targetAccount.hasChildren())
                                                                            result = targetAccount.id
                                                                    }
                                                                }


                                                                rootPane.apply {
                                                                    hgap = 10.0
                                                                    vgap = 10.0

                                                                    prefHeight = 600.0

                                                                    this.add(ComboBox<Category>().apply {
                                                                        items = categories

                                                                        this.converter = object : StringConverter<Category>() {
                                                                            override fun toString(`object`: Category?): String {
                                                                                return `object`!!.name
                                                                            }

                                                                            override fun fromString(string: String?): Category {
                                                                                return reporting.categories.find { it.name.equals(string) } as Category
                                                                            }

                                                                        }

                                                                        this.selectionModel.select(category)

                                                                        valueProperty().addListener { _, _, newVal ->
                                                                            category = newVal
                                                                        }

                                                                    }, 0, 0)

                                                                    group.attachToRoot(this)
                                                                }


                                                                dialogPane.content = rootPane

                                                                setResultConverter {
                                                                    if (it == ButtonType.OK) {
                                                                        val text = group.elements[0] as List<AutoCompleteTextField<Int>>
                                                                        val values = group.elements[valuePos] as List<NumericTextField>
                                                                        with(text.map { it.result }.zip(values.map { it.number })) {
                                                                            if (this.any { it.first != null && it.second != null }) {
                                                                                val e = Entry(category.nextEntryIndex, "", category).apply {
                                                                                    this@with.forEach { p ->
                                                                                        p.first?.let { id ->
                                                                                            p.second?.let {
                                                                                                this.add(id, it.toDouble())
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }

                                                                                if (!e.balanced()) {
                                                                                    e.unregister()
                                                                                    null
                                                                                } else {
                                                                                    e
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