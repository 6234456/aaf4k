package eu.qiou.aaf4k.util.unit

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.io.FxUtil
import eu.qiou.aaf4k.util.time.TimeParameters
import java.util.*

data class ForeignExchange(val functionalCurrency: Currency= GlobalConfiguration.DEFAULT_CURRENCY, val reportingCurrency: Currency = GlobalConfiguration.DEFAULT_CURRENCY, val timeParameters: TimeParameters) {
    var rate: Long? = null
    var decimalPrecision: Int = 5

    var displayRate: Double? = null
        get() = if(rate == null) null else rate!! / Math.pow(10.0, decimalPrecision.toDouble())
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

    fun fetchFxRate(forceRefresh:Boolean = false):Double {
        if (functionalCurrency.equals(reportingCurrency)){
            displayRate = 1.0
            return 1.0
        }

        return FxUtil.fetch(this, useCache = !forceRefresh)
    }

    override fun toString(): String {
        return "Exchange rate ${if(timePoint == null) "in ${timeSpan}" else  "on ${timePoint}"} of ${functionalCurrency.currencyCode}:${reportingCurrency.currencyCode} is ${String.format(GlobalConfiguration.DEFAULT_LOCALE, "%.${decimalPrecision}f", displayRate)}"
    }
}