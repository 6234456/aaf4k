package eu.qiou.aaf4k.util.unit

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.time.TimeParameters
import java.util.*
import kotlin.math.roundToInt


data class CurrencyUnit(override val scalar: UnitScalar = UnitScalar.UNIT, var currency: Currency =  GlobalConfiguration.DEFAULT_CURRENCY ) : ProtoUnit(scalar) {

    private val currencyCode:String = currency.currencyCode

    override fun format(locale: Locale): (Number) -> String {
        val f: (Number) -> String =
                  when(scalar){
                            UnitScalar.UNIT         ->  { n -> currencyCode + " " + String.format(locale, "%,.2f", n) }
                            else                    ->  { n -> currencyCode + " " + String.format(locale, "%,d", n.toDouble().roundToInt()) + " "+
                                when(scalar){
                                    UnitScalar.THOUSAND         -> "thousand"
                                    UnitScalar.TEN_THOUSAND     -> "ten thousand"
                                    UnitScalar.MILLION          -> "million"
                                    UnitScalar.HUNDRED_MILLION  -> "hundred million"
                                    UnitScalar.BILLION          -> "billion"
                                    else                        -> ""
                                }
                            }
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

    fun convertFxTo(targetCurrency: ProtoUnit, timeParameters: TimeParameters?=null): (Double) -> Double {
        if(!(targetCurrency is CurrencyUnit))
            throw Exception("Different Types are not convertible. $targetCurrency to Currency")

        if(currency.equals(targetCurrency.currency))
            return super.convertTo(targetCurrency)

        if(timeParameters == null)
            throw Exception("In case of foreign exchange, time parameters should be specified!")

        val fxRate = ForeignExchange(functionalCurrency = currency, reportingCurrency = targetCurrency.currency, timeParameters = timeParameters).fetch()

        val f : (Double) -> Double = {
            super.convertTo(targetCurrency)(it * fxRate)
        }

        return f
    }

    fun getSymbol():String {
        return "${scalar.token}${currencyCode}"
    }

}