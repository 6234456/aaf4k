package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.ECBFxFetcher
import eu.qiou.aaf4k.util.io.OandaFxFetcher
import eu.qiou.aaf4k.util.io.XEFxFetcher
import eu.qiou.aaf4k.util.strings.times
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.unit.ForeignExchange
import org.junit.Test
import java.time.LocalDate

class XEFxFetcherTest {

    @Test
    fun fetchFxFromSource() {
        val fetcher = XEFxFetcher()
        val fetcher1 = OandaFxFetcher()
        val ecb = ECBFxFetcher()

        val f1 = ForeignExchange("CNY","EUR", LocalDate.of(2018,4,11))

        val d1 = fetcher.fetchFxFromSource(f1)
        val d2 = fetcher1.fetchFxFromSource(f1)

        println("$d1  $d2")
        println("^*" * 21)

        val f2 = ForeignExchange("CNY","EUR", TimeSpan.forMonth(2018,1))

        println("${fetcher.fetchFxFromSource(f1)}  ${fetcher1.fetchFxFromSource(f1)} ${ecb.fetchFxFromSource(f1)}")

    }
}