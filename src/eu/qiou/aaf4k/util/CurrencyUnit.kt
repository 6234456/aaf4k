package eu.qiou.aaf4k.util

import java.text.NumberFormat
import java.util.*

class CurrencyUnit(scale: UnitScale = UnitScale.UNIT) : ProtoUnit(scale) {
    override fun format(locale: Locale): (Double) -> String {
        val f: (Double) -> String = { n -> NumberFormat.getCurrencyInstance(locale).format(n) }
        return f
    }

    override fun toString(): String {
        return "Currency: " + this.scale
    }
}