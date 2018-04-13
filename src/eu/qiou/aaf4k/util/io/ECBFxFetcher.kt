package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.util.time.TimeAttribute
import eu.qiou.aaf4k.util.unit.ForeignExchange
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.time.LocalDate


class ECBFxFetcher :FxFetcher() {

    override fun fetchFxFromSource(target: ForeignExchange): Double {
         var res = 0.0
         val m = parseURL(target)

         m.forEach { t, u ->
             res += u
         }

        return res / m.count()
    }

    private fun parseURL(target: ForeignExchange):Map<LocalDate, Double> {
       return Jsoup.parse(downloadXMLFile(buildURL(target)), "UTF-8", "", Parser.xmlParser())
               .select("generic:Series generic:Obs")
               .map{
                   LocalDate.parse(it.select("generic:ObsDimension").attr("value")) to it.select("generic:ObsValue").attr("value").toDouble()
               }.toMap()
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