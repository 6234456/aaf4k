package eu.qiou.aaf4k.gui

import eu.qiou.aaf4k.accounting.model.*
import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.roundUpTo
import eu.qiou.aaf4k.util.template.Template
import javafx.application.Application
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Side
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.util.StringConverter
import java.nio.file.Files
import java.nio.file.Paths

class GUI : Application() {

    companion object {
        fun open(reporting: Reporting) {

            GUI.reporting = reporting
            Application.launch(GUI::class.java)
        }

        lateinit var reporting: Reporting
    }

    override fun start(primaryStage: Stage?) {

        val reporting = GUI.reporting
        val reportingNull = reporting.nullify()

        val accountShown: (Account) -> String = { "${it.id} ${it.name}${if (it.hasParent()) "-" + it.superAccounts!![0].name else ""}" }
        val suggestions = reporting.flattenWithStatistical().map { accountShown(it) to it.id }.toMap()

        val categories = FXCollections.observableArrayList<Category>().apply {
            reporting.categories.map {
                this.add(it as Category)
            }
        }

        val formatter: (Number, Int) -> String = { n, dec ->
            if (Math.abs(n.toDouble()) < Math.pow(10.0, -1.0 * (dec + 1))) "" else String.format("%,.${dec}f", n.roundUpTo(dec))
        }

        val tab1 = Tab().apply {
            text = "科目汇总"
            isClosable = false
        }


        val tab2 = Tab().apply {
            text = "财务报表"
            isClosable = false

        }

        val tab3 = Tab().apply {
            text = "调整分录"
            isClosable = false
        }

        fun updateTab3() {
            with(tab3) {
                content = BorderPane().apply {
                    left = VBox().apply {
                        spacing = 8.0
                        categories.forEach {
                            it as Category
                            this.children.add(VBox().apply {
                                children.add(Text(it.name))
                                children.add(ListView<Entry>().apply {
                                    this.items.addAll((it.entries as List<Entry>).filter { it.isVisible })
                                })
                            })
                        }
                    }
                    val (sht, i) = ExcelUtil.getWorksheet("data/demo.xlsx", sheetIndex = 0)
                      right = XlTable(sht, true)
                    i.close()
                }
            }
        }

        updateTab3()

        fun updateTab1(selectedRow: Int? = null) {
            val root = TreeItem(
                    Account.from(ProtoAccount(0, reporting.entity.name, 0L), ReportingType.AUTO)
            ).apply {
                isExpanded = true
            }

            val treeView = TreeTableView<Account>(root)

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

                        var category = it as Category
                        TreeTableColumn<Account, String>(it.name).apply {
                            val data = reportingNull.update(it.toDataMap()).flattenWithAllAccounts().map { it.id to it.displayValue }.toMap()

                            setCellFactory {
                                object : TreeTableCell<Account, String>() {
                                    override fun updateItem(item: String?, empty: Boolean) {
                                        super.updateItem(item, empty)
                                        this.text = ""
                                        this.treeTableRow?.item?.let {
                                            this.text = formatter(data.getOrDefault(it.id, 0.0), it.decimalPrecision)
                                        }
                                    }
                                }.apply {
                                    this.setOnMouseClicked { e ->
                                        if (e.button == MouseButton.SECONDARY) {
                                            Paths.get("data/demo.xls").toFile().let {
                                                if (it.exists())
                                                    it.delete()
                                            }
                                            reporting.toXl("data/demo.xls", t = Template.Theme.BLACK_WHITE)
                                            println("exported")
                                        }


                                        if (e.button == MouseButton.PRIMARY) {
                                            if (e.clickCount == 2) {
                                                val targetAccount = this.treeTableRow?.item!!

                                                // booking mask
                                                val dialog: Dialog<Entry> = Dialog()

                                                dialog.run {
                                                    title = "Booking"
                                                    dialogPane.buttonTypes.addAll(
                                                            ButtonType.OK,
                                                            ButtonType.CANCEL
                                                    )

                                                    val rootPane = GridPane()

                                                    val description = SimpleStringProperty()

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

                                                        this.elements[0][0] = AutoCompleteTextField<Int>(if (!targetAccount.hasChildren()) accountShown(targetAccount) else "", suggestions).apply {
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
                                                                    return categories.find { it.name.equals(string) } as Category
                                                                }

                                                            }

                                                            this.selectionModel.select(category)

                                                            valueProperty().addListener { _, _, newVal ->
                                                                category = newVal
                                                            }

                                                        }, 0, 0)

                                                        this.add(TextField().apply {
                                                            this.textProperty().bindBidirectional(description)
                                                        }, 1, 0)

                                                        group.attachToRoot(this)
                                                    }


                                                    dialogPane.content = rootPane

                                                    setResultConverter {
                                                        if (it == ButtonType.OK) {
                                                            val text = group.elements[0] as List<AutoCompleteTextField<Int>>
                                                            val values = group.elements[valuePos] as List<NumericTextField>
                                                            with(text.map { it.result }.zip(values.map { it.number })) {
                                                                if (this.any { it.first != null && it.second != null }) {
                                                                    val e = Entry(category.nextEntryIndex, description.value
                                                                            ?: "", category).apply {
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
                                                                        (e.category as Category).summarizeResult()
                                                                        updateTab3()
                                                                        updateTab1(treeView.selectionModel.selectedIndex)
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
                                                        Files.write(Paths.get("data/accounting.txt"), reporting.toJSON().lines())
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
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

            with(tab1) {
                content = treeView.apply {
                    columns.addAll(
                            cols
                    )
                }
            }

            selectedRow?.let {
                treeView.selectionModel.select(it)
                treeView.scrollTo(it)
            }
        }

        updateTab1()

        with(primaryStage!!) {
            scene = Scene(
                    TabPane().apply {
                        side = Side.LEFT

                        tabs.add(
                                tab1
                        )
                        tabs.add(
                                tab2
                        )
                        tabs.add(
                                tab3
                        )
                    }


            )

            title = "Reporting"
            isFullScreen = true
            show()
        }

    }
}