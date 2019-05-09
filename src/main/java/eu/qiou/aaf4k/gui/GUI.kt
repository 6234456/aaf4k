package eu.qiou.aaf4k.gui

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.reportings.base.*
import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.io.toReporting
import eu.qiou.aaf4k.util.roundUpTo
import eu.qiou.aaf4k.util.template.Template
import javafx.application.Application
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.geometry.Side
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.layout.GridPane
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
            launch(GUI::class.java)
        }

        fun open(srcFile: String) {
            reporting = Files.readAllLines(Paths.get(srcFile)).joinToString("\n").toReporting()
            srcJSONFile = srcFile

            launch(GUI::class.java)
        }

        lateinit var reporting: Reporting
        var srcJSONFile: String? = null
        var locale: Locale = GlobalConfiguration.DEFAULT_LOCALE
            set(value) {
                field = if (supportedLocale.contains(value)) value else GlobalConfiguration.DEFAULT_LOCALE
            }
    }

    @Suppress("UNCHECKED_CAST")
    override fun start(primaryStage: Stage?) {

        val css = "file:///" + File("src/main/resources/stylesheet/main.css")
                .absolutePath.replace("\\", "/")

        val msg = ResourceBundle.getBundle("aaf4k", locale)
        Locale.setDefault(locale)

        val reporting = reporting

        val accountShown: (ProtoAccount) -> String = { "${it.id} ${it.name}${if (it is ProtoCollectionAccount) "-" + it.superAccounts[0].name else ""}" }
        val suggestions = reporting.sortedList().toSet().map { accountShown(it) to it.id }.toMap()

        val categories = FXCollections.observableArrayList<Category>().apply {
            reporting.categories.forEach {
                add(it)
            }
        }

        var toUpdateTab1 = false

        fun saveToJSON() {
            srcJSONFile?.let {
                Files.write(Paths.get(it), reporting.toJSON().lines())
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

        fun evokeBookingDialog(targetEntry: Entry? = null, targetAccount: ProtoAccount,
                               category: Category, updateViewCallback: (Category) -> Unit) {
            // booking mask
            val dialog: Dialog<Entry> = Dialog()
            var category1 = category

            dialog.run {
                title = msg.getString("booking")
                dialogPane.buttonTypes.addAll(
                        ButtonType.OK,
                        ButtonType.CANCEL
                )

                dialogPane.stylesheets.add(css)

                val rootPane = GridPane()

                val description = SimpleStringProperty(targetEntry?.desc)
                val indexPos = 0
                val valuePos = 1

                val group = ControlGroup(listOf(
                        { _: Int, _: ControlGroup ->
                            AutoCompleteTextField("", suggestions = suggestions).apply {
                                promptText = msg.getString("accountId")
                            }
                        },
                        { _: Int, _: ControlGroup ->
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

                                    f.forEach { it1 ->
                                        it1.bindingContext = f
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
                    inflate(if (targetEntry == null) 3 else targetEntry.accounts.size + 1)

                    if (targetEntry == null) {
                        (this.elements[indexPos][0] as AutoCompleteTextField<Long>).run {
                            setTextValue(if (targetAccount !is ProtoCollectionAccount) accountShown(targetAccount) else "")

                            if (targetAccount !is ProtoCollectionAccount) result = targetAccount.id
                        }
                    } else {
                        targetEntry.accounts.forEachIndexed { index, account ->
                            (this.elements[indexPos][index] as AutoCompleteTextField<Long>).run {
                                result = account.id
                                setTextValue(accountShown(account))
                            }
                            (this.elements[valuePos][index] as NumericTextField).run {
                                number = account.decimalValue
                                writeNumber(account.decimalValue)
                            }
                        }
                    }

                    val f = this.elements[valuePos] as List<NumericTextField>

                    f.forEach {
                        it.bindingContext = f
                    }
                }

                var entryDate: LocalDate = targetEntry?.date ?: category.timeParameters.end

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
                                return categories.find { it.name == string } as Category
                            }

                        }

                        this.selectionModel.select(category)

                        valueProperty().addListener { _, _, newVal ->
                            category1 = newVal
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

                        promptText = msg.getString("documentDate")

                        setOnAction {
                            entryDate = value ?: reporting.timeParameters.end
                        }
                    }, 0, 2)

                    group.attachToRoot(this)
                }

                dialogPane.content = rootPane

                setResultConverter {
                    if (it == ButtonType.OK) {
                        val text = group.elements[0] as List<AutoCompleteTextField<Long>>
                        val values = group.elements[valuePos] as List<NumericTextField>
                        with(text.map { field -> field.result }.zip(values.map { field -> field.number })) {
                            if (this.any { it1 -> it1.first != null && it1.second != null }) {
                                val e = Entry( description.value
                                        ?: "", category1, entryDate).apply {
                                    this@with.forEach { p ->
                                        p.first?.let { id ->
                                            p.second?.let { number ->
                                                this.add(id, number.toDouble())
                                            }
                                        }
                                    }
                                }

                                if ((!e.isBalanced) || e.accounts.all { account -> account.decimalValue == 0.0 }) {
                                    e.unregister()
                                    null
                                } else {
                                    targetEntry?.unregister()
                                    e.apply {
                                        targetEntry?.let { entry ->
                                            this.isActive = entry.isActive
                                            this.isWritable = entry.isWritable
                                            this.isVisible = entry.isVisible
                                        }
                                        this.category.summarizeResult()
                                        updateViewCallback(e.category)
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
                    saveToJSON()
                }
            }
        }

        fun updateTab3() {
            val contextMenu = ContextMenu()
            fun modifyMask(entry: Entry, acc: ProtoAccount) {
                evokeBookingDialog(targetEntry = entry, targetAccount = acc, category = entry.category) {
                    updateTab3()
                    toUpdateTab1 = true
                }
            }

            fun ctxMenu(entries: Iterable<Entry>, entry: Entry, acc: ProtoAccount): ContextMenu {
                return contextMenu.apply {
                    items.removeAll(items)
                    items.addAll(
                            MenuItem(msg.getString("editBooking")).apply {
                                setOnAction {
                                    modifyMask(entry, acc)
                                }
                            },
                            MenuItem(
                                    if (entry.isActive) msg.getString("deactivateBooking") else msg.getString("activateBooking")
                            ).apply {
                                setOnAction {
                                    val res = !entry.isActive
                                    entries.forEach { entry1 -> entry1.isActive = res }
                                    entry.category.summarizeResult()
                                    updateTab3()
                                    toUpdateTab1 = true
                                    saveToJSON()
                                }
                            },
                            SeparatorMenuItem(),
                            MenuItem(msg.getString("deleteBooking")).apply {
                                setOnAction {
                                    with(Alert(Alert.AlertType.CONFIRMATION).apply {
                                        contentText = msg.getString("warningUnreversable")
                                        headerText = msg.getString("deleteBooking")
                                        title = msg.getString("deleteBooking")
                                    }.showAndWait()) {
                                        if (this.get() == ButtonType.OK) {
                                            entries.forEach { entry1 -> entry1.unregister() }
                                            entry.category.summarizeResult()
                                            updateTab3()
                                            toUpdateTab1 = true
                                            saveToJSON()
                                        }
                                    }
                                }
                            }
                    )
                }
            }

            val contentTable = ListView<Entry>().apply {
                selectionModel.selectionMode = SelectionMode.MULTIPLE

                setCellFactory {
                    object : ListCell<Entry>() {
                        override fun updateItem(item: Entry?, empty: Boolean) {
                            super.updateItem(item, empty)

                            if (item != null && !empty)
                                this.text = item.toString()
                            else {
                                text = null
                                graphic = null
                            }
                        }
                    }.apply {
                        if (this.item != null && !this.item.isActive) {
                            this.styleClass.add("inactive")
                        }

                        this.setOnMouseClicked { e ->
                            val entry = this.item
                            val entries = this.listView.selectionModel.selectedItems

                            entry?.accounts?.get(0)?.let { acc ->
                                if (e.button == MouseButton.PRIMARY) {
                                    if (e.clickCount == 2) {
                                        modifyMask(entry, acc)
                                    }
                                }
                                if (e.button == MouseButton.SECONDARY) {
                                    ctxMenu(entries, entry, acc).show(this, e.screenX, e.sceneY)
                                }
                            }
                        }
                    }
                }
            }

            fun updateContent(entries: List<Entry>) {
                if (entries.isNotEmpty()) {
                    contentTable.apply {
                        items.removeAll(contentTable.items)
                        for (entry in entries) {
                            if (entry.isVisible)
                                items.add(entry)
                        }
                        refresh()
                    }
                }
            }

            val root = TreeItem(msg.getString("adjustments")).apply {
                isExpanded = true
            }
            categories.forEach {
                val c = it
                root.children.add(TreeItem("${it.id} ${it.name}").apply {
                    isExpanded = true
                    if (it.entries.isNotEmpty())
                        children.addAll(
                                it.entries.filter { entry -> entry.isVisible }.map { entry -> TreeItem("${c.id}.${entry.id} ${entry.desc}") }
                        )
                })
            }

            fun parseToEntry(s: String): Pair<Entry?, Category>? {
                val reg = """^(\d+)(\.(\d+))?\s(.*)$""".toRegex()

                if (reg.matches(s)) {
                    val g = reg.find(s)!!.groups
                    val c = categories.find { it.id == g[1]!!.value.toInt() }!!

                    if (g[3] == null)
                        return null to c

                    return c.entries.find { it.id == g[3]!!.value.toInt() } as Entry to c
                }

                return null
            }

            val treeView = TreeView<String>(root).apply {

                selectionModel.selectionMode = SelectionMode.MULTIPLE

                selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                    //based on the text value to get the Entry
                    if (contextMenu.isShowing) {
                        contextMenu.hide()
                    }

                    parseToEntry(newValue.value)?.let {
                        val (e, c) = it
                        if (e == null) {
                            updateContent(c.entries as List<Entry>)
                        } else {
                            updateContent(listOf(e))
                        }
                    }
                }

                setOnMouseClicked { e ->
                    if (selectionModel.selectedItem != null) {
                        parseToEntry(selectionModel.selectedItem.value)?.let {
                            val entry = it.first
                            if (entry != null) {
                                if (e.button == MouseButton.PRIMARY) {
                                    if (e.clickCount == 2) {
                                        modifyMask(entry, entry.accounts[0])
                                    }
                                }
                                if (e.button == MouseButton.SECONDARY) {
                                    ctxMenu(selectionModel.selectedItems.map { treeItem -> parseToEntry(treeItem.value)?.first }.filter { entry1 -> entry1 != null } as Iterable<Entry>, entry, entry.accounts[0]).show(this, e.screenX, e.sceneY)
                                }
                            }
                        }
                    }
                }
            }

            with(tab3) {
                content = SplitPane().apply {
                    orientation = Orientation.HORIZONTAL
                    setDividerPositions(0.25)
                    items.addAll(
                            treeView,
                            contentTable
                    )
                }
            }
        }

        updateTab3()

        fun updateTab2() {
            val overview = "data/demo.xls"

            if(Files.exists(Paths.get(overview)))
                with(tab2) {
                    val (sht, i) = ExcelUtil.getWorksheet("data/demo.xls", sheetIndex = 0)
                    content = XlTable(sht, true)
                    i.close()
                }
        }

        updateTab2()

        fun updateTab1(selectedRow: Int? = null) {

            val root = TreeItem(
                    reporting
                ).apply {
                    isExpanded = true
                } as TreeItem<ProtoAccount>

            val treeView = TreeTableView<ProtoAccount>(root)

            fun inflateTreeItem(item: TreeItem<ProtoAccount> = root, accounts: List<ProtoAccount> = reporting.structure) {
                accounts.forEach {
                    val parent = TreeItem(it)

                    item.children.add(parent)
                    item.isExpanded = true

                    if (it is ProtoCollectionAccount) {
                        inflateTreeItem(parent, it.subAccounts)
                    }
                }
            }

            inflateTreeItem()

            val cols = listOf(
                    TreeTableColumn<ProtoAccount, String>(msg.getString("accountId")).apply {
                        setCellValueFactory {
                            ReadOnlyStringWrapper(it.value.value.id.toString())
                        }
                    },
                    TreeTableColumn<ProtoAccount, String>(msg.getString("accountName")).apply {
                        setCellValueFactory {
                            ReadOnlyStringWrapper(
                                    with(it.value.value) {
                                        if (isStatistical) "${msg.getString("thereOf")}:$name" else name
                                    }
                            )
                        }

                    },
                    TreeTableColumn<ProtoAccount, String>(msg.getString("balanceBeforeAdj")).apply {
                        setCellValueFactory {
                            ReadOnlyStringWrapper(formatter(it.value.value.displayValue, it.value.value.decimalPrecision))
                        }

                        style = "-fx-alignment:center-right"
                    }
            ) +
                    reporting.categories.map {
                        val category = it
                        TreeTableColumn<ProtoAccount, String>(it.name).apply {
                            val data = it.toDataMap()
                            setCellFactory {
                                object : TreeTableCell<ProtoAccount, String>() {
                                    override fun updateItem(item: String?, empty: Boolean) {
                                        super.updateItem(item, empty)
                                        this.text = ""
                                        this.treeTableRow?.item?.let { account ->
                                            this.text = formatter(data.getOrDefault(account.id, 0.0), account.decimalPrecision)
                                        }
                                    }
                                }.apply {
                                    this.setOnMouseClicked { e ->
                                        val targetAccount = this.treeTableRow?.item!!
                                        if (e.button == MouseButton.SECONDARY) {
                                            val contextMenu =
                                                    ContextMenu().apply {
                                                        this.items.addAll(
                                                                MenuItem(msg.getString("booking")).apply {
                                                                    setOnAction {
                                                                        evokeBookingDialog(targetAccount = targetAccount, category = category) {
                                                                            updateTab3()
                                                                            updateTab1(treeView.selectionModel.selectedIndex)
                                                                        }
                                                                    }
                                                                },
                                                                MenuItem(msg.getString("viewBookings")).apply {
                                                                    setOnAction {
                                                                        println(category.entries.filter { x-> x.accounts.contains(targetAccount) })
                                                                    }
                                                                },
                                                                SeparatorMenuItem(),
                                                                Menu("${msg.getString("exportAs")}...").apply {
                                                                    items.addAll(
                                                                            MenuItem(".xls").apply {
                                                                                setOnAction {
                                                                                    Paths.get("data/demo.xls").toFile().let { file ->
                                                                                        if (file.exists())
                                                                                            file.delete()
                                                                                    }
                                                                                    reporting.shorten().toXl("data/demo.xls", t = Template.Theme.DEFAULT, locale = locale)
                                                                                    println("exported")
                                                                                }
                                                                            },
                                                                            MenuItem(".xlsx").apply {
                                                                                setOnAction {
                                                                                    Paths.get("data/demo.xlsx").toFile().let { file ->
                                                                                        if (file.exists())
                                                                                            file.delete()
                                                                                    }
                                                                                    reporting.shorten().toXl("data/demo.xlsx", t = Template.Theme.DEFAULT, locale = locale)
                                                                                    println("exported")
                                                                                }
                                                                            }
                                                                    )
                                                                }
                                                        )
                                                    }

                                            contextMenu.show(this, e.screenX, e.screenY)
                                        }
                                        if (e.button == MouseButton.PRIMARY) {
                                            if (e.clickCount == 2) {
                                                evokeBookingDialog(targetAccount = targetAccount, category = category) {
                                                    updateTab3()
                                                    updateTab1(treeView.selectionModel.selectedIndex)
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
                            TreeTableColumn<ProtoAccount, String>(msg.getString("balanceAfterAdj")).apply {
                                val data = reporting.toDataMap()
                                setCellValueFactory {
                                    ReadOnlyStringWrapper(
                                            formatter(data.getOrDefault(it.value.value.id, 0.0),
                                                    it.value.value.decimalPrecision)
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
                                tab1.apply {
                                    setOnSelectionChanged {
                                        if (toUpdateTab1) {
                                            updateTab1()
                                            toUpdateTab1 = false
                                        }
                                    }
                                }
                        )
                        tabs.add(
                                tab2
                        )
                        tabs.add(
                                tab3
                        )
                    }
            ).apply {
                stylesheets.add(css)
            }

            title = msg.getString("reporting")
            isMaximized = true
            show()
        }

    }
}