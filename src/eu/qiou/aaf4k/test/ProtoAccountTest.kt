package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.ProtoAccount
import eu.qiou.aaf4k.util.strings.CollectionToString
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.PercentageUnit
import eu.qiou.aaf4k.util.unit.UnitScalar
import org.junit.Test
import java.util.*

class ProtoAccountTest{
    @Test
    fun testProtoAccount(){
        val account  = ProtoAccount(id = 1234, name = "test", value = 120100, unit = CurrencyUnit(currency = Currency.getInstance("CNY")))

        val account1  = ProtoAccount(id = 1234, name = "test", value = 1214, unit = PercentageUnit.getInstance())
        account1.displayUnit= PercentageUnit.getInstance()

        println(account.displayUnit)

        account.displayUnit = CurrencyUnit(UnitScalar.THOUSAND)
        println(account)

        account.displayUnit = CurrencyUnit(UnitScalar.UNIT, currency = Currency.getInstance("EUR"))
        account.timeParameters = TimeParameters.realTime()
        println(account)

        println(account1)
    }
}