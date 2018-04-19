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

        assert(account2.displayValue == 22.0)

        println(account2)
    }

    @Test
    fun testBuilder(){
        val a1 = ProtoAccount.builder().setBasicInfo(1234, "test1").setValue(1234.45, 2).build()
        val a2 = ProtoAccount.builder().setBasicInfo(1235, "test1").setValue(2323445).build()
        val a3 = ProtoAccount.builder().setBasicInfo(123, "test1").setValue(mutableSetOf(a1, a2)).build()

        println(a3.toJSON())
    }

    @Test
    fun testUpdate() {
        val a1 = ProtoAccount(1, "a1", mutableSetOf(ProtoAccount(2, "a2", 1000L)))
        val a3 = ProtoAccount(3, "a3", 2000L)
        a1.add(a3)

        val a0 = ProtoAccount(0, "total")
        a0.add(a1)
        a0.add(a3)
        println(a0.deepCopy(mapOf(2 to 200.0, 3 to 100.0)))
    }
}