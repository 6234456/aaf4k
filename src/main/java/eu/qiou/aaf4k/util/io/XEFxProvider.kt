package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.util.time.TimeAttribute
import eu.qiou.aaf4k.util.unit.ForeignExchange
import org.jsoup.Jsoup
import java.time.temporal.ChronoUnit

object XEFxProvider : FxProvider() {
    override fun fetchFxFromSource(target: ForeignExchange): Double {
        val urls = buildURL(target)
        var failsCnt = 0

        return urls.fold(0.0) { acc, s ->
            try {
                acc + parseURL(s, target)
            } catch (e: IndexOutOfBoundsException) {
                failsCnt++
                acc + 0
            }

        } / (urls.count() - failsCnt)
    }

    private fun parseURL(url: String, target: ForeignExchange): Double {
        val targetCurrency = target.reportingCurrency.currencyCode

        val document = Jsoup.connect(url).get()
        val element = document.select("#historicalRateTbl tbody tr td a").filter { it.html().trim() == targetCurrency }[0]

        return element.parent().siblingElements()[1].html().toDouble()
    }


    private fun buildURL(target: ForeignExchange): List<String> {

        val baseCurrency = target.functionalCurrency.currencyCode

        return when (target.timeParameters.timeAttribute) {
            TimeAttribute.TIME_POINT -> listOf("https://www.xe.com/currencytables/?from=$baseCurrency&date=${target.timeParameters.timePoint}")
            TimeAttribute.TIME_SPAN -> target.timeParameters.timeSpan!!.drillDown(1, ChronoUnit.DAYS).map { "https://www.xe.com/currencytables/?from=$baseCurrency&date=${it.end}" }
            else -> throw Exception("time profile defined error!")
        }
    }
}