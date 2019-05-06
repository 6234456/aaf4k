package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.base.Account
import eu.qiou.aaf4k.reportings.base.CollectionAccount
import eu.qiou.aaf4k.reportings.base.ReportingType
import org.junit.Test

class AccountTestBase {

    @Test
    fun trail0() {
        val account = Account(1234, "demo", value = 5)
        val account2 = CollectionAccount(2345, "Demo2").apply {
            add(account)
        }
        val account3 = CollectionAccount(2346, "Demo3", isStatistical = true).apply {
            add(account2)
            add(account.copy(id = 2347))
            add(account)
            add(account.copy(id = 2347, isStatistical = true))
        }

        println(account2.value)
        println(account3.value)
        println(account3.deepCopy())

    }

    @Test
    fun trail2() {
        val acc = Account(123, "demo1", value = 123456, reportingType = ReportingType.ASSET_LONG_TERM)
        val acc2 = CollectionAccount(1234, "Demo").apply {
            this += acc
        }

        println(acc2.deepCopy())
        println(acc2.sortedAllList)
    }
}