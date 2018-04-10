package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.AggregateAccount
import eu.qiou.aaf4k.reportings.ProtoAccount
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.UnitScalar
import org.junit.Test
import java.util.*


class AggregateAccountTest {
    @Test
    fun addSubAccount() {

        val acc1 = ProtoAccount(1234, "acc1",120, CurrencyUnit(UnitScalar.THOUSAND), decimalPrecision = 0, timeParameters = TimeParameters.realTime())
        val acc2 = ProtoAccount(1235, "acc2",220000, timeParameters = TimeParameters.realTime(), displayUnit = CurrencyUnit(currency = Currency.getInstance("EUR")))
        val acc3 = ProtoAccount(1238, "acc3",220000, timeParameters = TimeParameters.realTime())
        val agg1 = AggregateAccount(1236, "agg1")
        val agg2 = AggregateAccount(1237, "agg2")

        agg2.add(acc3)
        agg1.add(agg2)
        agg2.add(acc1)

        agg1 += acc1
        agg1 += acc2

      /*  println(agg1.displayUnit)
        println(agg1.displayValue)
        println(agg1)

        println(agg1.checkDistinct())

        println(agg2.superAccount == agg1)
        println(acc2.displayValue)
        println(agg1)
        println(agg1.checkDuplicated())
    */

        println(acc3 in agg1)
        println(agg1.removeRecursively(acc3))
        println(acc3.superAccounts)
    }
}