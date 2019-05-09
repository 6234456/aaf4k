package eu.qiou.aaf4k.gui

import eu.qiou.aaf4k.gui.StringParser.parseBindingString
import eu.qiou.aaf4k.gui.StringParser.regBindingElement
import eu.qiou.aaf4k.util.roundUpTo
import eu.qiou.aaf4k.util.strings.times
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.TextField
import java.text.DecimalFormat
import java.text.NumberFormat
import javax.script.ScriptEngineManager

// bindingContext specifies a range of components to be chosen from
class NumericTextField(val decimalPrecision: Int, text: String? = "", var bindingContext: List<NumericTextField>? = null) : TextField(text) {
    companion object {
        private val parser = (NumberFormat.getNumberInstance() as DecimalFormat).decimalFormatSymbols
        private val thousandSep = parser.groupingSeparator
        private val decimalSep = parser.decimalSeparator
        private val regFormula = """^=([\$\(\)\$decimalSep\+\-\*/\d]*)\s*$""".toRegex()
        private val regNormal = """\-?\d*\$decimalSep?\d*""".toRegex()

        private val js = ScriptEngineManager().getEngineByName("js")
        private val formatter: (Number, Int) -> String = { n, dec ->
            if (Math.abs(n.toDouble()) < Math.pow(10.0, -1.0 * (dec + 1))) "0" else String.format("%,.${dec}f", n.roundUpTo(dec))
        }
        private val formatterWithoutSep: (Number, Int) -> String = { n, dec ->
            if (Math.abs(n.toDouble()) < Math.pow(10.0, -1.0 * (dec + 1))) "0" else String.format("%.${dec}f", n.roundUpTo(dec))
        }


    }

    private val formatParser = (NumberFormat.getNumberInstance() as DecimalFormat).apply {
        applyPattern("#,###.${"0" * decimalPrecision}")
    }
    // bindingString starts with $()
    // $1 position of the target element in the srcList
    private fun bindingWith(bindingString: String, bindingContext: List<NumericTextField>? = this.bindingContext): (() -> Double) {
        return parseBindingString(bindingString.replace(thousandSep.toString(), "").replace(decimalSep.toString(), "."), NumericTextField::doubleValue, bindingContext!!) {
            this.bind(it)
        }
    }

    var formula: String? = null
    private var fixed: Boolean = false
    var number: Number? = null
    private fun doubleValue(): Double {
        return if (number == null) 0.0 else number!!.toDouble()
    }

    val observerList: MutableList<NumericTextField> = mutableListOf()
    val srcList: MutableList<NumericTextField> = mutableListOf()


    // two options to bind: one input in the textfield
    private fun parseString(t: String, decimalPrecision: Int): Double {
        return try {
            if (t.isBlank()) {
                formula = null
                unbind()
                0.0
            } else if (regFormula.matches(t)) {
                val e = regFormula.find(t)!!.groups[1]!!.value
                if (regBindingElement.containsMatchIn(e)) {
                    if (formula != t) {
                        if (bindingContext == null)
                            throw Exception("BindingContext should be specified!")

                        bindingString = t
                        formula = t
                    }

                    bindingMethod()

                } else {
                    formula = t
                    js.eval(
                            e.replace(thousandSep.toString(), "").replace(decimalSep.toString(), ".")
                    ).toString().toDouble()
                }
            } else {
                formula = null
                unbind()
                formatParser.parse(t).toDouble()
            }
        } catch (x: javax.script.ScriptException) {
            0.0
        }.roundUpTo(decimalPrecision)
    }

    // or set bindingString programmtically
    private lateinit var bindingMethod: (() -> Double)
    var bindingString: String? = null
        set(value) {
            unbind()
            if (value != null) {
                bindingMethod = bindingWith(value)
            }
        }

    fun circleBinded(other: NumericTextField): Boolean {
        return (other == this) || other.srcList.any { it.circleBinded(this) }
    }

    fun bind(other: NumericTextField) {
        if (!circleBinded(other)) {
            srcList.add(other)
            other.observerList.add(this)
        } else {
            formula = null
            number = 0
        }
    }

    fun unbind() {
        srcList.forEach { it.observerList.remove(this) }
        srcList.clear()
    }

    private fun updateOnBinding() {
        Platform.runLater {
            writeNumber(bindingMethod())
        }
    }

    private fun notifyObservers() {
        observerList.forEach {
            it.updateOnBinding()
        }
    }

    fun writeNumber(n: Number) {
        fixed = true
        number = n
        this.text = if (n == 0.0) "" else formatter(number!!, decimalPrecision)
        notifyObservers()
    }

    private fun formatText() {
        text?.let { t ->
            writeNumber(parseString(t, decimalPrecision))
        }
    }

    init {
        textProperty().addListener { _, o, t ->
            if (!t.isBlank()) {
                if (!fixed && !(regFormula.matches(t) || regNormal.matches(t))) {
                    this.text = o
                }
            }
        }

        focusedProperty().addListener { _, _, n ->
            if (!n)
                formatText()
            else {
                fixed = false
                if (formula != null)
                    this.text = formula
                else
                    this.text = if (number == null || number == 0.0) "" else formatterWithoutSep(number!!, decimalPrecision)
            }
        }

        this.alignment = Pos.CENTER_RIGHT
    }
}