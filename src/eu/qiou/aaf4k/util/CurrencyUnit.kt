package eu.qiou.aaf4k.util

import java.util.*
import kotlin.math.roundToInt

data class CurrencyUnit(override val scale: UnitScale = UnitScale.UNIT, var currency: Currency =  Currency.getInstance("EUR") ) : ProtoUnit(scale) {
    override fun format(locale: Locale): (Double) -> String {
        val f: (Double) -> String =
                  when(scale){
                            UnitScale.UNIT -> { n -> String.format(locale, "%,.2f", n) }
                            else -> { n -> String.format(locale, "%,d", n.roundToInt()) }
                        }
        return f
    }

    override fun toString(): String {
        return "Currency: in ${getSymbol()}"
    }

    override fun convertTo(unit: ProtoUnit): (Double) -> Double {
        if(!(unit is CurrencyUnit))
            throw Exception("Different Types are not convertible. $unit to Currency")
        return super.convertTo(unit)
    }

    fun getSymbol():String {
        return "${scale.token}${currency.getSymbol()}"
    }

}