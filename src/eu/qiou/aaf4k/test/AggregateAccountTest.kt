package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.AggregateAccount
import eu.qiou.aaf4k.reportings.ProtoAccount
import eu.qiou.aaf4k.reportings.ProtoReportingInfo
import org.junit.Test

import org.junit.Assert.*

class AggregateAccountTest {

    @Test
    fun addSubAccount() {
        val acc1 = ProtoAccount(1234, "acc1",120000)
        val acc2 = ProtoAccount(1235, "acc2",220000)
        val acc3 = ProtoAccount(1238, "acc2",220000)
        val agg1 = AggregateAccount(1236, "agg1")
        val agg2 = AggregateAccount(1237, "agg1")

        agg1.addSubAccount(acc1)
        agg1.addSubAccount(acc2)
        agg1.addSubAccount(acc3)
        agg1.addSubAccount(agg2)
        agg2.addSubAccount(acc1)

        println(agg1.displayUnit)
        println(agg1.value)
        println(agg1)

        println(agg1.checkDistinct())

        println(agg2.superAccount == agg1)



    }
}