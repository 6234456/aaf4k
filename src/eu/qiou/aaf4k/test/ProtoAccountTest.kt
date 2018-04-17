package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.model.ProtoAccount
import org.junit.Test

class ProtoAccountTest{
    @Test
    fun testProtoAccount(){
        val account0 = ProtoAccount(id=0, name="acc1", value = 1000, desc = "test1")
        val account1 = ProtoAccount(id=1, name="acc1", value = 1200, desc = "test1")
        val account2 = ProtoAccount(id=1, name="acc2", subAccounts = mutableSetOf(account0, account1), desc = "test1")

        assert(account1 == account2)
        assert(!account1.hasChildren())

        assert(account2.hasChildren())
        assert(account2.value == 2200L)
        assert(account2.displayValue == 22.0)

        account2.displayValue = 2.1
        assert(account2.displayValue == 22.0)


        println(account2)

    }
}