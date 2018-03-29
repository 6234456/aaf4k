package eu.qiou.aaf4k.util

import java.util.*
import kotlin.math.roundToInt

class CurrencyUnit(scale: UnitScale = UnitScale.UNIT) : ProtoUnit(scale) {
    override fun format(locale: Locale): (Double) -> String {
        val f: (Double) -> String =
                  when(scale){
                            UnitScale.UNIT -> { n -> String.format(locale, "%,.2f", n) }
                            else -> { n -> String.format(locale, "%,d", n.roundToInt()) }
                        }
        return f
    }

    override fun toString(): String {
        return "Currency: " + this.scale
    }
}