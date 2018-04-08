package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.ForeignExchange
import org.junit.Test

import java.util.*

class ForeignExchangeTest {
    @Test
    fun getDecimalPrecision() {
        val fx = ForeignExchange(functionalCurrency = Currency.getInstance("EUR"), timeParameters = TimeParameters.realTime())
        val fx1 = ForeignExchange(functionalCurrency = Currency.getInstance("EUR"), timeParameters = TimeParameters.realTime())
        fx.decimalPrecision = 3
        fx.fetch()
        fx1.fetch()

        println(fx)
        println(fx1)
        println(fx1 == fx)
    }
}