package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.ProtoAccount
import eu.qiou.aaf4k.util.io.FxUtil
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.time.times
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ForeignExchange
import eu.qiou.aaf4k.util.unit.UnitScalar
import org.junit.Test

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

class FxUtilTest {

    @Test
    fun fetch() {
        val s = (1..3).map {
            ForeignExchange(functionalCurrency = Currency.getInstance("EUR"), reportingCurrency = Currency.getInstance("CNY"), timeParameters = TimeParameters(timePoint = LocalDate.of(2018,1,it)))
        }

        s.forEach({it -> FxUtil.fetch(it)})

        println(s)



        // Convert

        val acc1 = ProtoAccount(1000, "CNY", 100000000, CurrencyUnit(currency = Currency.getInstance("CNY")),2,"",
                TimeParameters(timePoint = LocalDate.of(2018,1,1)), CurrencyUnit( scalar = UnitScalar.THOUSAND ,currency = Currency.getInstance("EUR")))

        println(acc1)


        // Tap the boundary of Oanda
        var cnt = 170

        (cnt..200).map { LocalDate.now() - ChronoUnit.DAYS * it }
                .map { ForeignExchange(functionalCurrency = Currency.getInstance("EUR"), reportingCurrency = Currency.getInstance("CNY"), timeParameters = TimeParameters(timePoint = it)) }
                .forEach {
                    try {
                        FxUtil.fetch(it)
                        println(it)
                    } catch(e:Exception){
                        println("Error - $cnt: $it ")
                    }
                    cnt++
                }
    }


    @Test
    fun fetchAvg(){
        val fx = ForeignExchange("EUR",  "EUR", TimeSpan.forYear(2018))
        val fx1 = ForeignExchange("EUR",  "USD", TimeSpan.forMonth(2018,3))
        val fx3 = ForeignExchange("EUR",  "CHF", TimeSpan.forMonth(2018,3))
        val fx0 = ForeignExchange("USD",  "CNY", TimeSpan.forMonth(2018,3))
        val fx2 = ForeignExchange("EUR",  "USD", LocalDate.now() - ChronoUnit.DAYS * 1)

        println(fx)
        println(fx1)
        println(fx0)
        println(fx2)
        println(fx3)
    }

}