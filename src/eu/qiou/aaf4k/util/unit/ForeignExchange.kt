package eu.qiou.aaf4k.util.unit

import eu.qiou.aaf4k.reportings.accounting.Accounting
import eu.qiou.aaf4k.util.io.FxUtil
import eu.qiou.aaf4k.util.time.TimeParameters
import java.util.*

class ForeignExchange(val functionalCurrency: Currency= Accounting.DEFAULT_CURRENCY, val reportingCurrency: Currency = Accounting.DEFAULT_CURRENCY, var rate: Long? = null, val decimalPrecision: Int = 5, val timeParameters: TimeParameters) {
    var displayRate: Double? = null
        get() = rate!! / Math.pow(10.0, decimalPrecision.toDouble())
        set(v) {
            rate = Math.round(v!! * Math.pow(10.0, decimalPrecision.toDouble()))
            field = v
        }

    private val timePoint = timeParameters.timePoint
    private val timeSpan = timeParameters.timeSpan

    init {
        if (functionalCurrency.equals(reportingCurrency)){
            displayRate = 1.0
        }
    }

    fun fetchFxRate():Double {
        if (functionalCurrency.equals(reportingCurrency)){
            return 1.0
        }

        return FxUtil.fetch(this)
    }

    override fun toString(): String {
        return "Exchange rate ${if(timePoint == null) "in ${timeSpan}" else  "on ${timePoint}"} of ${reportingCurrency.currencyCode}:${functionalCurrency.currencyCode} is ${String.format(Accounting.DEFAULT_LOCALE, "%.${decimalPrecision}f", displayRate)}."
    }
}