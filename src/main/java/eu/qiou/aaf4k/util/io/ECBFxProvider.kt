package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.util.time.TimeAttribute
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.unit.ForeignExchange
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.time.LocalDate
import java.util.*

/**
 * fetch exchange rate record from data service of ECB
 *
 * @author Qiou Yang
 *
 */
object ECBFxProvider : FxProvider() {

    override fun fetchFxFromSource(target: ForeignExchange): Double {
        val res = parseURL(target)
        if (target.reportingCurrency == Currency.getInstance("EUR"))
            return 1 / res

        return res
    }

    override fun baseFx(target: ForeignExchange): Map<LocalDate, Double> {
        if (target.timeParameters.timeAttribute == TimeAttribute.TIME_SPAN) {
            val url = buildURL(target)
            val v1 = hashMapOf<Int, Double>()

            JSONUtil.fetch<JSONObject>(url, false, "dataSets.0.series.0:0:0:0:0.observations").forEach { k, x ->
                v1[k.toString().toInt()] = (x as JSONArray)[0] as Double
            }

            return JSONUtil.fetch<JSONArray>(url, false, "structure.dimensions.observation.0.values").map { v ->
                LocalDate.parse((v as JSONObject)["name"].toString())
            }
                    .zip(
                            v1.toSortedMap().values
                    )
                    .map { it.first to it.second }.toMap()
        }

        var d = target.timeParameters.start
        val m1 = LocalDate.of(d.year, d.monthValue, 1)
        val m = TimeParameters(TimeSpan(m1.minusMonths(1), m1.plusMonths(1).minusDays(1)))
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

        if (!(baseCurrency == "EUR" || targetCurrency == "EUR")) {
            throw Exception("Only EUR related exchange rate supported.")
        } else if (baseCurrency == "EUR") {
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