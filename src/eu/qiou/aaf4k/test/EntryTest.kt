package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.accounting.model.*
import eu.qiou.aaf4k.reportings.model.ProtoAccount
import org.junit.Test

class EntryTest {

    @Test
    fun balanced() {
        val acc1 = Account.from(ProtoAccount.Builder().setBasicInfo(1234, "Asset1").setValue(10000).build(), ReportingType.ASSET)
        val acc2 = Account.from(ProtoAccount.Builder().setBasicInfo(1235, "Asset2").setValue(200.0, 2).build(), ReportingType.ASSET)
        val acc3 = Account.from(ProtoAccount.Builder().setBasicInfo(1236, "Liab1").setValue(-150.0).build(), ReportingType.LIABILITY)
        val acc4 = Account.from(ProtoAccount.Builder().setBasicInfo(1237, "Equity").setValue(-150.0).build(), ReportingType.EQUITY)
        val acc0 = Account(0, "DemoAssets", mutableSetOf(acc1, acc2), reportingType = ReportingType.ASSET)

        val reporting = Reporting(123, "Test", "A Test Reporting", listOf(acc0, acc3, acc4))
        val c = Category("Demo", 1, "", reporting)
        val e = Entry(1, "Trail", c)

        e.add(1234, 100.0)
        e.add(1235, -100.0)

        assert(e.balanced())
        assert(acc3.reportingValue.equals(150.0))

        println(reporting.toString())
        println(reporting.update(e))


    }

}