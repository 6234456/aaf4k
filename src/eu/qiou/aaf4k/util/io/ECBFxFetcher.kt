package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.util.time.TimeAttribute
import eu.qiou.aaf4k.util.unit.ForeignExchange
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.util.*


class ECBFxFetcher :FxFetcher() {

    override fun fetchFxFromSource(target: ForeignExchange): Double {
        var res = parseURL(target)
        if(target.reportingCurrency.equals(Currency.getInstance("EUR")))
            return 1/res

        return res
    }

    /**
     * TODO( average of dataSets needed )
     */
    private fun parseURL(target: ForeignExchange):Double {
        println(JSONUtil.fetch<Any>(buildURL(target),false, ""))
        return JSONUtil.fetch(buildURL(target),false, "dataSets.0.series.0:0:0:0:0.observations.0.0")

    }

    private fun downloadXMLFile(url:String): FileInputStream{
        val name = "tmp.xml"

        val website = URL(url)
        val rbc = Channels.newChannel(website.openStream())
        val fos = FileOutputStream(name)
        fos.channel.transferFrom(rbc, 0, java.lang.Long.MAX_VALUE)
        fos.flush()
        fos.close()

        val fis = FileInputStream(name)
        return fis
    }

    private fun buildURL(target: ForeignExchange):String{

        var baseCurrency = target.functionalCurrency.currencyCode
        val targetCurrency = target.reportingCurrency.currencyCode

        if(! (baseCurrency.equals("EUR") || targetCurrency.equals("EUR")) ){
            throw Exception("Only EUR related exchange rate supported.")
        }else if(baseCurrency.equals("EUR")){
            baseCurrency = targetCurrency
        }

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