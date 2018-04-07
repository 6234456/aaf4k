package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.reportings.GlobalConfiguration.FX_OANDA_QUERY_STRING
import eu.qiou.aaf4k.util.time.TimeAttribute
import eu.qiou.aaf4k.util.unit.ForeignExchange
import org.json.simple.JSONArray


/**
 * Oanda supports only inquiries of the recent 180 days
 * val url = "https://www.oanda.com/fx-for-business/historical-rates/api/data/update/?&source=OANDA&adjustment=0&base_currency=CNY&start_date=2017-10-8&end_date=2018-4-6&period=daily&price=bid&view=graph&quote_currency_0=EUR"
 */
class OandaFxFetcher:FxFetcher() {

    override fun fetchFxFromSource(target: ForeignExchange): Double {

        // Check the queryString for update
        val arr = JSONUtil.fetch<JSONArray>(queryString = FX_OANDA_QUERY_STRING, source = buildURL(target))

        if(target.timeParameters.timeAttribute == TimeAttribute.TIME_POINT){
            if(! (arr.get(0) as Long).toDate().equals(target.timeParameters.timePoint))
                throw Exception("Date out of scope: Oanda does not support the inquiry of ${target.timeParameters.timePoint}")
        }

        // TODO("add support for inquiry of data range")

        return (arr.get(1) as String).toDouble()
    }

    private fun buildURL(target: ForeignExchange):String{

        val baseCurrency = target.functionalCurrency.currencyCode
        val targetCurrency = target.reportingCurrency.currencyCode

        val startDate = when(target.timeParameters.timeAttribute){
            TimeAttribute.TIME_POINT -> target.timeParameters.timePoint
            TimeAttribute.TIME_SPAN -> target.timeParameters.timeSpan!!.start
            else -> throw Exception("time profile defined error!")
        }

        val endDate = when(target.timeParameters.timeAttribute){
            TimeAttribute.TIME_POINT -> target.timeParameters.timePoint
            TimeAttribute.TIME_SPAN -> target.timeParameters.timeSpan!!.end
            else -> throw Exception("time profile defined error!")
        }

        return "https://www.oanda.com/fx-for-business/historical-rates/api/data/update/?&source=OANDA&adjustment=0&base_currency=${baseCurrency}&start_date=${startDate}&end_date=${endDate}&period=daily&price=bid&view=graph&quote_currency_0=${targetCurrency}"
    }
}