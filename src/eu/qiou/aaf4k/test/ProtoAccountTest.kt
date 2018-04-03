package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.ProtoAccount
import eu.qiou.aaf4k.util.CurrencyUnit
import eu.qiou.aaf4k.util.PercentageUnit
import eu.qiou.aaf4k.util.UnitScale
import org.junit.Test
import java.util.*

class ProtoAccountTest{
    @Test
    fun testProtoAccount(){
        val account  = ProtoAccount(id = 1234, name = "test", value = 120100, unit = CurrencyUnit(currency = Currency.getInstance("CNY")))
        account.displayUnit = CurrencyUnit(UnitScale.THOUSAND)

        val account1  = ProtoAccount(id = 1234, name = "test", value = 1214, unit = PercentageUnit.getInstance())
        account1.displayUnit= PercentageUnit.getInstance()

        println(account.displayUnit)

        println(account)

        println(account1)
    }
}