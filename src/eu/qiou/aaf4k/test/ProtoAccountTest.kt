package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.accounting.model.Account
import eu.qiou.aaf4k.accounting.model.ReportingType
import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.util.io.ECBFxProvider
import eu.qiou.aaf4k.util.io.FromJSON
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ForeignExchange
import org.junit.Test

class ProtoAccountTest{
    @Test
    fun testProtoAccount(){
        val account0 = ProtoAccount(id=0, name="acc1", value = 1000, desc = "test1")
        val account1 = ProtoAccount(id=1, name="acc1", value = 1200, desc = "test1")
        val account2 = ProtoAccount(id = 1, name = "acc2", subAccounts = mutableListOf(account0, account1), desc = "test1")
        val account3 = ProtoAccount(id = 3, name = "acc3", subAccounts = mutableListOf(account2, account0))
        assert(account1 == account2)
        assert(!account1.hasChildren())

        assert(account2.hasChildren())
        assert(account2.displayValue == 22.0)

        assert(account2.displayValue == 22.0)

        println(account3)
    }

    @Test
    fun testBuilder(){
        val a1 = ProtoAccount.builder().setBasicInfo(1234, "test1").setValue(1234.45, 2).build()
        val a2 = ProtoAccount.builder().setBasicInfo(1235, "test1").setValue(2323445).build()
        val a3 = ProtoAccount.builder().setBasicInfo(123, "test1").setValue(mutableListOf(a1, a2)).build()

        println(a3.toJSON())
    }

    @Test
    fun testUpdate() {
        val a1 = ProtoAccount(1, "a1", mutableListOf(ProtoAccount(2, "a2", 1000L)))
        val a3 = ProtoAccount(3, "a3", 2000L)
        a1.add(a3)

        val a0 = ProtoAccount(0, "total")
        a0.add(a1)
        a0.add(a3)


        println(Account.from(a3, ReportingType.LIABILITY).toJSON())
        println(FromJSON.account(FromJSON.read(Account.from(a3, ReportingType.LIABILITY).toJSON())))
    }

    @Test
    fun testFx() {
        val a = ProtoAccount(1, "demo1", 100000000, 4, CurrencyUnit("CNY"), timeParameters = TimeParameters.forYear(2018))
        val b = ProtoAccount(2, "demo2", 100000000, 4, CurrencyUnit("USD"), timeParameters = TimeParameters.forYear(2018))
        val c = ProtoAccount(3, "acc", subAccounts = mutableListOf(a, b))


        val fx = ECBFxProvider.fetchFxFromSource(ForeignExchange("CNY", "EUR", TimeSpan.forYear(2018)))
        val fx1 = ECBFxProvider.fetchFxFromSource(ForeignExchange("USD", "EUR", TimeSpan.forYear(2018)))

        println(a.decimalValue)
        println(a.value)
        println("RMB : ${1 / fx}")

        println(b.decimalValue)
        println("USD : ${1 / fx1}")

        println(c.decimalValue)
    }
}