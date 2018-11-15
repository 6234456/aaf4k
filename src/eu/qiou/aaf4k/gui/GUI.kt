package eu.qiou.aaf4k.gui

import eu.qiou.aaf4k.accounting.model.*
import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.io.toReporting
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
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.util.*

// TODO: tempo entry
// TODO: template entries
class GUI : Application() {

    companion object {
        val supportedLocale: List<Locale> = listOf(
                Locale.CHINESE, Locale.ENGLISH, Locale.GERMAN
        )

        fun open(reporting: Reporting) {
            GUI.reporting = reporting
            Application.launch(GUI::class.java)
        }

        fun open(srcFile: String) {
            GUI.reporting = Files.readAllLines(Paths.get(srcFile)).joinToString("\n").toReporting()
            srcJSONFile = srcFile

            Application.launch(GUI::class.java)
        }

        lateinit var reporting: Reporting
        var srcJSONFile: String? = null
        var locale: Locale = GlobalConfiguration.DEFAULT_LOCALE
            set(value) {
                field = if (supportedLocale.contains(value)) value else GlobalConfiguration.DEFAULT_LOCALE
            }
    }

    override fun start(primaryStage: Stage?) {

        val msg = ResourceBundle.getBundle("aaf4k", GUI.locale)
        Locale.setDefault(GUI.locale)

        val reporting = GUI.reporting
        val reportingNull = reporting.nullify()

        val accountShown: (Account) -> String = { "${it.id} ${it.name}${if (it.hasParent()) "-" + it.superAccounts!![0].name else ""}" }
        val suggestions = reporting.flattenWithStatistical().toSet().map { accountShown(it) to it.id }.toMap()

        val categories = FXCollections.observableArrayList<Category>().apply {
            reporting.categories.map {
                this.add(it as Category)
            }
        }

        val formatter: (Number, Int) -> String = { n, dec ->
            if (Math.abs(n.toDouble()) < Math.pow(10.0, -1.0 * (dec + 1))) "" else String.format("%,.${dec}f", n.roundUpTo(dec))
        }

        val tab1 = Tab().apply {
            text = msg.getString("generalLedger")
            isClosable = false
        }


        val tab2 = Tab().apply {
            text = msg.getString("financialStatements")
            isClosable = false

        }

        val tab3 = Tab().apply {
            text = msg.getString("adjustments")
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
                    val (sht, i) = ExcelUtil.getWorksheet("data/demo.xls", sheetIndex = 0)
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
                    TreeTableColumn<Account, String>(msg.getString("accountId")).apply {
                        setCellValueFactory {
                            ReadOnlyStringWrapper(it.value.value.id.toString())
                        }
                    },
                    TreeTableColumn<Account, String>(msg.getString("accountName")).apply {
                        setCellValueFactory {
                            ReadOnlyStringWrapper(
                                    with(it.value.value) {
                                        if (isStatistical) "${msg.getString("thereOf")}:$name" else name
                                    }
                            )
                        }

                    },
                    TreeTableColumn<Account, String>(msg.getString("balanceBeforeAdj")).apply {
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
                                            Paths.get("data/demo.xlsx").toFile().let {
                                                if (it.exists())
                                                    it.delete()
                                            }
                                            reporting.shorten().toXl("data/demo.xlsx", t = Template.Theme.DEFAULT, locale = GUI.locale)

                                            Paths.get("data/demo.xls").toFile().let {
                                                if (it.exists())
                                                    it.delete()
                                            }
                                            reporting.shorten().toXl("data/demo.xls", t = Template.Theme.DEFAULT, locale = GUI.locale)
                                            println("exported")
                                        }


                                        if (e.button == MouseButton.PRIMARY) {
                                            if (e.clickCount == 2) {
                                                val targetAccount = this.treeTableRow?.item!!

                                                // booking mask
                                                val dialog: Dialog<Entry> = Dialog()

                                                dialog.run {
                                                    title = msg.getString("booking")
                                                    dialogPane.buttonTypes.addAll(
                                                            ButtonType.OK,
                                                            ButtonType.CANCEL
                                                    )

                                                    dialogPane.stylesheets.add("file:///" + File("stylesheet/main.css").absolutePath.replace("\\", "/"))

                                                    val rootPane = GridPane()

                                                    val description = SimpleStringProperty()

                                                    val valuePos = 1

                                                    val group = ControlGroup(listOf(
                                                            { _: Int, g: ControlGroup ->
                                                                AutoCompleteTextField<Int>("", suggestions = suggestions).apply {
                                                                    promptText = msg.getString("accountId")
                                                                }
                                                            },
                                                            { _: Int, g: ControlGroup ->
                                                                NumericTextField(targetAccount.decimalPrecision).apply {
                                                                    promptText = msg.getString("bookingPHValue")
                                                                }
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
                                                                    setOnAction {
                                                                        g.append(g.elements[i].indexOf(this), rootPane)

                                                                        val f = (g.elements[valuePos] as List<NumericTextField>)

                                                                        f.forEach {
                                                                            it.bindingContext = f
                                                                        }
                                                                    }
                                                                }
                                                            },
                                                            { i: Int, g: ControlGroup ->
                                                                Button("-").apply {
                                                                    setOnAction { g.remove(g.elements[i].indexOf(this), rootPane) }
                                                                }
                                                            }
                                                    ), 0, 5).apply {
                                                        inflate(3)

                                                        this.elements[0][0] = AutoCompleteTextField<Int>(if (!targetAccount.hasChildren()) accountShown(targetAccount) else "", suggestions).apply {
                                                            if (!targetAccount.hasChildren())
                                                                result = targetAccount.id
                                                        }

                                                        val f = (this.elements[valuePos] as List<NumericTextField>)

                                                        f.forEach {
                                                            it.bindingContext = f
                                                        }
                                                    }

                                                    var entryDate: LocalDate = category.timeParameters.end

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
                                                            promptText = msg.getString("desc")
                                                        }, 0, 1)

                                                        this.add(Button("R").apply {
                                                            setOnAction {
                                                                (group.elements[valuePos] as MutableList<NumericTextField>).forEach { x ->
                                                                    if (x.number != null) {
                                                                        x.writeNumber(x.number!!.toDouble() * -1)
                                                                    }
                                                                }
                                                            }
                                                        }, 4, 0)

                                                        this.add(DatePicker(entryDate).apply {
                                                            setDayCellFactory {
                                                                object : DateCell() {
                                                                    override fun updateItem(item: LocalDate?, empty: Boolean) {
                                                                        super.updateItem(item, empty)
                                                                        isDisable = empty || item == null || !category.timeParameters.contains(item)
                                                                    }
                                                                }
                                                            }

                                                            promptText = "Date of Entry"

                                                            setOnAction {
                                                                entryDate = value
                                                            }
                                                        }, 0, 2)

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
                                                                            ?: "", category, entryDate).apply {
                                                                        this@with.forEach { p ->
                                                                            p.first?.let { id ->
                                                                                p.second?.let {
                                                                                    this.add(id, it.toDouble())
                                                                                }
                                                                            }
                                                                        }
                                                                    }

                                                                    if ((!e.balanced()) || e.accounts.all { it.decimalValue == 0.0 }) {
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
                                                        srcJSONFile?.let {
                                                            Files.write(Paths.get(it), reporting.toJSON().lines())
                                                        }
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
                            TreeTableColumn<Account, String>(msg.getString("balanceAfterAdj")).apply {
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
            ).apply {
                stylesheets.add("file:///" + File("stylesheet/main.css").absolutePath.replace("\\", "/"))
            }

            title = msg.getString("reporting")
            isFullScreen = true
            show()
        }

    }
}