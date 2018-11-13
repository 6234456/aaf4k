package eu.qiou.aaf4k.gui

import eu.qiou.aaf4k.gui.StringParser.parseBindingString
import eu.qiou.aaf4k.gui.StringParser.regBindingElement
import eu.qiou.aaf4k.util.roundUpTo
import javafx.application.Platform
import javafx.scene.control.TextField
import javax.script.ScriptEngineManager

// bindingContext specifies a range of components to be chosen from
class NumericTextField(val decimalPrecision: Int, text: String? = "", var bindingContext: List<NumericTextField>? = null) : TextField(text) {
    companion object {
        private val regFormula = """^=([\$\(\)\.\+\-\*/\d]*)\s*$""".toRegex()
        private val regNormal = """\-?\d*\.?\d*""".toRegex()

        private val js = ScriptEngineManager().getEngineByName("js")
        private val formatter: (Number, Int) -> String = { n, dec ->
            if (Math.abs(n.toDouble()) < Math.pow(10.0, -1.0 * (dec + 1))) "0" else String.format("%,.${dec}f", n.roundUpTo(dec))
        }
        private val formatterWithoutSep: (Number, Int) -> String = { n, dec ->
            if (Math.abs(n.toDouble()) < Math.pow(10.0, -1.0 * (dec + 1))) "0" else String.format("%.${dec}f", n.roundUpTo(dec))
        }

    }

    // bindingString starts with $()
    // $1 position of the target element in the srcList
    private fun bindingWith(bindingString: String, bindingContext: List<NumericTextField>? = this.bindingContext): (() -> Double) {
        return parseBindingString(bindingString, NumericTextField::doubleValue, bindingContext!!) {
            this.bind(it)
        }
    }

    var formula: String? = null
    private var fixed: Boolean = false
    var number: Number? = null
    private fun doubleValue(): Double {
        return if (number == null) 0.0 else number!!.toDouble()
    }

    private val observerList: MutableList<NumericTextField> = mutableListOf()
    private val srcList: MutableList<NumericTextField> = mutableListOf()


    // two options to bind: one input in the textfield
    private fun parseString(t: String, decimalPrecision: Int): Double {
        return try {
            if (regFormula.matches(t)) {
                formula = t
                val e = regFormula.find(t)!!.groups[1]!!.value
                if (regBindingElement.containsMatchIn(e)) {
                    if (bindingContext == null)
                        throw Exception("BindingContext should be specified!")
                    bindingString = t
                    bindingMethod()

                } else {
                    js.eval(e).toString().toDouble()
                }
            } else {
                t.toDouble()
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

    fun bind(other: NumericTextField) {
        srcList.add(other)
        other.observerList.add(this)
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
        this.text = formatter(number!!, decimalPrecision)
        notifyObservers()
    }


    private fun formatText() {
        text?.let { t ->
            if (!t.isBlank()) {
                writeNumber(parseString(t, decimalPrecision))
            }
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
                    this.text = if (number == null) "" else formatterWithoutSep(number!!, decimalPrecision)
            }
        }
    }
}