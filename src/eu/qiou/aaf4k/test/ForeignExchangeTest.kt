package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.ECBFxProvider
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.ForeignExchange
import org.junit.Test
import java.time.LocalDate

class ForeignExchangeTest {
    @Test
    fun getDecimalPrecision() {
        ForeignExchange.autoFetch = true
        val fx = ForeignExchange(reportingCurrencyCode = "CHF", timePoint = LocalDate.of(2018, 5, 16))
        val fx1 = ForeignExchange(reportingCurrencyCode = "GBP", timePoint = LocalDate.of(2018, 5, 16))
        val fx2 = ForeignExchange(reportingCurrencyCode = "USD", timePoint = LocalDate.of(2018, 5, 16))
        println(fx)
        println(fx1)
        println(fx2)
    }

    @Test
    fun getBaseFx() {
        val f = ECBFxProvider.baseFx(ForeignExchange(reportingCurrencyCode = "CNY", timeParameters = TimeParameters.forQuarter(2018, 2)))
        println(f)

        ForeignExchange.source = ECBFxProvider
        println(ForeignExchange(reportingCurrencyCode = "CNY", timePoint = LocalDate.of(2018, 6, 29)))
        println(ForeignExchange(reportingCurrencyCode = "CNY", timePoint = LocalDate.of(2018, 4, 23)))
    }
}