package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.ProtoAccount
import eu.qiou.aaf4k.util.io.FxUtil
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ForeignExchange
import eu.qiou.aaf4k.util.unit.UnitScalar
import org.junit.Test

import java.time.LocalDate
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
    }
}