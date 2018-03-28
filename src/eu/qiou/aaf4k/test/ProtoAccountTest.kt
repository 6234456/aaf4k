package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.ProtoAccount
import eu.qiou.aaf4k.util.CurrencyUnit
import eu.qiou.aaf4k.util.PercentageUnit
import eu.qiou.aaf4k.util.UnitScale
import org.junit.Test

class ProtoAccountTest{
    @Test
    fun testProtoAccount(){
        val account : ProtoAccount = ProtoAccount(id = 1234, name = "test", value = 120100, displayUnit = CurrencyUnit(UnitScale.THOUSAND))
        val account1 : ProtoAccount = ProtoAccount(id = 1234, name = "test", value = 1214, unit = PercentageUnit())
        println(account.displayUnit)
        println(account)

        account.displayUnit= CurrencyUnit()
        println(account)

        println(account1)
    }
}