package eu.qiou.aaf4k.test

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
}