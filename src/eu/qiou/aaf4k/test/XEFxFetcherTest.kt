package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.ECBFxFetcher
import eu.qiou.aaf4k.util.io.OandaFxFetcher
import eu.qiou.aaf4k.util.io.XEFxFetcher
import eu.qiou.aaf4k.util.strings.times
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.unit.ForeignExchange
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

class XEFxFetcherTest {

    @Test
    fun fetchFxFromSource() {
        val fetcher = XEFxFetcher
        val fetcher1 = OandaFxFetcher
        val ecb = ECBFxFetcher

        val f1 = ForeignExchange("EUR","CNY", LocalDate.of(2017,12,27))

        val d1 = fetcher.fetchFxFromSource(f1)
        val d2 = fetcher1.fetchFxFromSource(f1)
        val d3 = ecb.fetchFxFromSource(f1)

        println("$d1  $d2  $d3")
        println("^*" * 21)
    }


    @Test
    fun fetchFxECB() {

        ForeignExchange.autoFetch = false

        val ecb = ECBFxFetcher
        val f1 = ForeignExchange("EUR","CNY", TimeSpan.forMonth(2018,2))
        val f2 = ForeignExchange("EUR","CNY", LocalDate.of(2018,3, 22))
        val f3 = ForeignExchange("EUR","CNY", TimeSpan.forYear(2017))

        val oanda = OandaFxFetcher

        println("${ecb.fetchFxFromSource(f1)}  ${oanda.fetchFxFromSource(f1)}")
        println("${f2.fetch(src = ECBFxFetcher)}  ${f2.fetch(OandaFxFetcher)}")
        println("${f2.fetch(src = ECBFxFetcher)}  ${f2.fetch(OandaFxFetcher)}")
        println("${f3.fetch(src = ECBFxFetcher)} ")

    }



    @Test
    fun redirectTest(){
        val url = "http://sdw-wsrest.ecb.europa.eu/service/data/EXR/M.USD.EUR.SP00.A"

        val obj =  URL(url)
        var conn = obj.openConnection() as HttpURLConnection
        conn.readTimeout = 5000
        conn.setRequestProperty("Accept", "application/json")

        System.out.println("Request URL ... " + url)

        // normally, 3xx is redirect
        val status = conn.responseCode

        println(status.toString() + " " + conn.content)
    }
}