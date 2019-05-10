package eu.qiou.aaf4k.util.unit

import eu.qiou.aaf4k.reportings.GlobalConfiguration.DEFAULT_CURRENCY_CODE
import eu.qiou.aaf4k.reportings.GlobalConfiguration.DEFAULT_FUNCTIONAL_CURRENCY
import eu.qiou.aaf4k.reportings.GlobalConfiguration.DEFAULT_LOCALE
import eu.qiou.aaf4k.util.io.ECBFxProvider
import eu.qiou.aaf4k.util.io.FxProvider
import eu.qiou.aaf4k.util.io.FxUtil
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.time.TimeSpan
import java.time.LocalDate
import java.util.*

data class ForeignExchange(val functionalCurrency: Currency = DEFAULT_FUNCTIONAL_CURRENCY, val reportingCurrency: Currency = DEFAULT_FUNCTIONAL_CURRENCY, val timeParameters: TimeParameters) {

    constructor(functionalCurrencyCode: String = DEFAULT_CURRENCY_CODE, reportingCurrencyCode: String = DEFAULT_CURRENCY_CODE, timeSpan: TimeSpan):this(functionalCurrency = Currency.getInstance(functionalCurrencyCode), reportingCurrency = Currency.getInstance(reportingCurrencyCode), timeParameters = TimeParameters(timeSpan))
    constructor(functionalCurrencyCode: String = DEFAULT_CURRENCY_CODE, reportingCurrencyCode: String = DEFAULT_CURRENCY_CODE, timePoint: LocalDate):this(functionalCurrency = Currency.getInstance(functionalCurrencyCode), reportingCurrency = Currency.getInstance(reportingCurrencyCode), timeParameters = TimeParameters(timePoint))
    constructor(functionalCurrencyCode: String = DEFAULT_CURRENCY_CODE, reportingCurrencyCode: String = DEFAULT_CURRENCY_CODE, timeParameters: TimeParameters) : this(functionalCurrency = Currency.getInstance(functionalCurrencyCode), reportingCurrency = Currency.getInstance(reportingCurrencyCode), timeParameters = timeParameters)

    private var rate: Long? = null
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
        if (autoFetch){
            fetch()
        }
    }

    fun fetch(src: FxProvider = source, forceRefresh: Boolean = ForeignExchange.forceRefresh): Double {
        if (functionalCurrency == reportingCurrency) {
            displayRate = 1.0
            return 1.0
        }

        return FxUtil.fetch(this, source = src, useCache = !forceRefresh)
    }

    override fun toString(): String {
        return "Exchange rate ${if (timePoint == null) "in $timeSpan" else "on $timePoint"} of ${functionalCurrency.currencyCode}:${reportingCurrency.currencyCode} is ${String.format(DEFAULT_LOCALE, "%.${decimalPrecision}f", displayRate)}"
    }

    companion object {
        var autoFetch = true
        var forceRefresh = false
        var source: FxProvider = ECBFxProvider
    }
}

