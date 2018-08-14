package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.util.time.TimeAttribute
import eu.qiou.aaf4k.util.unit.ForeignExchange
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.util.*

/**
 * fetch exchange rate record from data service of ECB
 *
 * @author Qiou Yang
 *
 */
object ECBFxProvider : FxProvider() {

    override fun fetchFxFromSource(target: ForeignExchange): Double {
        var res = parseURL(target)
        if (target.reportingCurrency.equals(Currency.getInstance("EUR")))
            return 1 / res

        return res
    }

    override fun baseFx(target: ForeignExchange): Map<java.time.LocalDate, Double> {


        if (target.timeParameters.timeAttribute == TimeAttribute.TIME_SPAN) {
            val url = buildURL(target)
            val v1 = hashMapOf<Int, Double>()

            JSONUtil.fetch<JSONObject>(url, false, "dataSets.0.series.0:0:0:0:0.observations").forEach({ k, x ->
                v1.put(k.toString().toInt(), (x as JSONArray).get(0) as Double)
            })

            return JSONUtil.fetch<JSONArray>(url, false, "structure.dimensions.observation.0.values").map({ v ->
                java.time.LocalDate.parse((v as JSONObject).get("name").toString())
            })
                    .zip(
                            v1.toSortedMap().values
                    )
                    .map { it.first to it.second }.toMap()
        }

        var d = target.timeParameters.start
        val m = target.timeParameters.containingMonth()
        with(baseFx(target.copy(timeParameters = m))) {
            while (m.timeSpan!!.contains(d) and !this.containsKey(d)) {
                d = d.minusDays(1)
            }

            if (m.timeSpan.contains(d)) {
                return hashMapOf(d to this.getValue(d))
            }
        }

        throw Exception("FX not found!")
    }

    private fun parseURL(target: ForeignExchange): Double {
        with(baseFx(target).values) {
            return this.fold(0.0, { acc, d -> acc + d }) / this.count()
        }
    }

    private fun buildURL(target: ForeignExchange): String {

        var baseCurrency = target.functionalCurrency.currencyCode
        val targetCurrency = target.reportingCurrency.currencyCode

        if (!(baseCurrency.equals("EUR") || targetCurrency.equals("EUR"))) {
            throw Exception("Only EUR related exchange rate supported.")
        } else if (baseCurrency.equals("EUR")) {
            baseCurrency = targetCurrency
        }

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

        /**
         * https://sdw-wsrest.ecb.europa.eu/service/data/EXR/D.CNY.EUR.SP00.A?startPeriod=2018-04-11&endPeriod=2018-04-12&detail=dataonly
         *
         * D            daily basis
         * SP00         fx-service
         * A            average
         *
         * see> https://sdw-wsrest.ecb.europa.eu/web/generator/index.html
         */
        return "https://sdw-wsrest.ecb.europa.eu/service/data/EXR/D.$baseCurrency.EUR.SP00.A?startPeriod=$startDate&endPeriod=$endDate"
    }
}