package eu.qiou.aaf4k.util.unit

import eu.qiou.aaf4k.reportings.accounting.Accounting
import eu.qiou.aaf4k.util.time.TimeAttribute
import eu.qiou.aaf4k.util.time.TimeSpan
import java.time.LocalDate
import java.util.*

class ForeignExchange(val functionalCurrency: Currency= Accounting.DEFAULT_CURRENCY, val reportingCurrency: Currency = Accounting.DEFAULT_CURRENCY, var rate: Long? = null, val decimalPrecision: Int = 5, val timeSpan: TimeSpan?=null, val timePoint: LocalDate?=null) {
    var displayRate: Double? = null
        get() = rate!! / Math.pow(10.0, decimalPrecision.toDouble())
        set(v) {
            rate = Math.round(v!! * Math.pow(10.0, decimalPrecision.toDouble()))
            field = v
        }

    val timeAttribute: TimeAttribute = when{
        timeSpan    != null && timePoint    == null     -> TimeAttribute.TIME_SPAN
        timePoint   != null && timeSpan     == null     -> TimeAttribute.TIME_POINT
        else -> throw Exception("Specification Error: One and only one of the attribute timeSpan/timePoint should be specified!")
    }

    init {
        if (functionalCurrency.equals(reportingCurrency)){
            displayRate = 1.0
        }
    }

    override fun toString(): String {
        return "Exchange rate ${if(timePoint == null) "in ${timeSpan}" else  "on ${timePoint}"} ${reportingCurrency.currencyCode}:${functionalCurrency.currencyCode} is ${String.format(Accounting.DEFAULT_LOCALE, "%.${decimalPrecision}f", displayRate)}."
    }
}