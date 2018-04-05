package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.ForeignExchange
import org.junit.Test

import java.time.LocalDate
import java.util.*

class ForeignExchangeTest {
    @Test
    fun getDecimalPrecision() {
        val fx = ForeignExchange(functionalCurrency = Currency.getInstance("EUR"), timeParameters = TimeParameters.realTime(), decimalPrecision = 4)
        fx.displayRate = 1.123456
        println(fx.timeParameters)
        println(fx)
    }
}