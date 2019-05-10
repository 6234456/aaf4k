package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.reportings.GlobalConfiguration.FX_OANDA_QUERY_STRING_ATOMIC
import eu.qiou.aaf4k.reportings.GlobalConfiguration.FX_OANDA_QUERY_STRING_DATA_ARRAY
import eu.qiou.aaf4k.util.time.TimeAttribute
import eu.qiou.aaf4k.util.unit.ForeignExchange
import org.json.simple.JSONArray
import java.time.LocalDate

/**
 * Oanda supports only inquiries of the recent 180 days
 * val url = "https://www.oanda.com/fx-for-business/historical-rates/api/data/update/?&source=OANDA&adjustment=0&base_currency=CNY&start_date=2017-10-8&end_date=2018-4-6&period=daily&price=bid&view=graph&quote_currency_0=EUR"
 */
object OandaFxProvider : FxProvider() {

    override fun fetchFxFromSource(target: ForeignExchange): Double {

        when (target.timeParameters.timeAttribute) {
            TimeAttribute.TIME_POINT -> {

                val arr = JSONUtil.fetch<JSONArray>(queryString = FX_OANDA_QUERY_STRING_ATOMIC, source = buildURL(target))
                checkDateEqual(arr, target.timeParameters.timePoint!!)
                return getValueFromAtomicJSONArray(arr)

            }
            TimeAttribute.TIME_SPAN -> {
                val arr = JSONUtil.fetch<JSONArray>(queryString = FX_OANDA_QUERY_STRING_DATA_ARRAY, source = buildURL(target)).toList()

                checkDateEqual(arr[0] as JSONArray, target.timeParameters.timeSpan!!.end)
                checkDateEqual(arr.last() as JSONArray, target.timeParameters.timeSpan.start)

                return arr.fold(0.0) { a, b -> a + getValueFromAtomicJSONArray(b as JSONArray) } / arr.count()

            }
            else -> throw Exception("Unknown time attribute for foreign exchange: ${target.timeParameters.timeAttribute}")
        }
    }

    private fun checkDateEqual(atomicJSONArray: JSONArray, targetDate: LocalDate) {
        if ((atomicJSONArray[0] as Long).toDate() != targetDate)
            throw Exception("Date out of scope: Oanda does not support the inquiry of $targetDate")
    }

    private fun getValueFromAtomicJSONArray(atomicJSONArray: JSONArray): Double {
        return (atomicJSONArray[1] as String).toDouble()
    }

    private fun buildURL(target: ForeignExchange): String {

        val baseCurrency = target.functionalCurrency.currencyCode
        val targetCurrency = target.reportingCurrency.currencyCode

        val startDate = when (target.timeParameters.timeAttribute) {
            TimeAttribute.TIME_POINT -> target.timeParameters.timePoint
            TimeAttribute.TIME_SPAN -> target.timeParameters.timeSpan!!.start
            else -> throw Exception("time profile defined error!")
        }

        val endDate = when (target.timeParameters.timeAttribute) {
            TimeAttribute.TIME_POINT -> target.timeParameters.timePoint
            TimeAttribute.TIME_SPAN -> target.timeParameters.timeSpan!!.end
            else -> throw Exception("time profile defined error!")
        }

        return "https://www.oanda.com/fx-for-business/historical-rates/api/data/update/?&source=OANDA&adjustment=0&base_currency=$baseCurrency&start_date=$startDate&end_date=$endDate&period=daily&price=bid&view=graph&quote_currency_0=$targetCurrency"
    }
}