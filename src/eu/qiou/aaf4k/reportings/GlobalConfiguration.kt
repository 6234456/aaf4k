package eu.qiou.aaf4k.reportings

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


    val DEFAULT_LOCALE: Locale = Locale.GERMANY
    val DEFAULT_CURRENCY: Currency = Currency.getInstance("CNY")



    // Foreign Exchange Rate

    // 1. Oanda

    // get the atom data array in json, like[2018-04-06, 0.12936]
    val FX_OANDA_QUERY_STRING = "widget.0.data.0"
    val FX_OANDA_URL_FORMAT = ""
}