package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.reportings.base.Entity
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.UnitScalar
import java.util.*

/**
 * Parameters for Testing
 * Later configured through cfg-file
 * TODO("re-implemented in cfg")
 */

object GlobalConfiguration {

    /**
     *  Pre-defined account number for period result
     */
    val RESULT_ACCOUNT_ID : Int = 0
    val DEFAULT_DECIMAL_PRECISION: Int = 2
    val DEFAULT_FX_DECIMAL_PRECISION: Int = 4


    val DEFAULT_LOCALE: Locale = Locale.CHINA
    val DEFAULT_FUNCTIONAL_CURRENCY: Currency = Currency.getInstance("EUR")
    val DEFAULT_CURRENCY_CODE: String = DEFAULT_FUNCTIONAL_CURRENCY.currencyCode
    val DEFAULT_CURRENCY_UNIT: CurrencyUnit = CurrencyUnit(scalar = UnitScalar.UNIT, currency = DEFAULT_FUNCTIONAL_CURRENCY)

    val DEFAULT_TIME_PARAMETERS = TimeParameters(2017)
    val DEFAULT_REPORTING_ENTITY = Entity(0, "Demo GmbH", "Demo", "a fictive company")

    val DEFAULT_FONT_NAME = "Bahnschrift"
    val DEFAULT_PROJECT_NAME = "JAP 2018"
    val DEFAULT_PROCESSOR_NAME = "Qiou Yang"

    val DEFAULT_AUTHOR_NAME = "Qiou Yang"

    // Foreign Exchange Rate

    // 1. Oanda

    // get the atom data array in json, like[2018-04-06, 0.12936]
    const val FX_OANDA_QUERY_STRING_ATOMIC = "widget.0.data.0"
    // get JSONArray of atomic data
    const val FX_OANDA_QUERY_STRING_DATA_ARRAY = "widget.0.data"
    val FX_OANDA_URL_FORMAT = ""
}