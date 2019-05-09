package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.base.*
import org.junit.Test

class EntryTest {

    @Test
    fun balanced() {
        val acc1 = Account(1234, "Asset1", 10000, reportingType = ReportingType.ASSET)
        val acc2 = Account(1235, "Asset2", reportingType = ReportingType.ASSET).copyWith(234.45)
        val acc3 = Account(1236, "Liab1", reportingType = ReportingType.LIABILITY).copyWith(-150.0)
        val acc4 = Account(1236, "Equity", reportingType = ReportingType.EQUITY).copyWith(-150.0)
        val acc0 = CollectionAccount(0, "DemoAssets", reportingType = ReportingType.ASSET).apply {
            this + acc1
            this + acc2
        }

        val reporting = Reporting(CollectionAccount(123, "Test", desc = "A Test Reporting").apply {
            this + acc0
            this + acc3
            this + acc4
        })
        println(acc0)
        println(reporting)

        val c = Category("Demo", "", reporting)
        val e = Entry("Trail", c)

        e.add(1234, 100.0)
        e.add(1235, -100.0)

        assert(e.isBalanced)
        assert(acc3.reportingValue.equals(150.0))

        e.add(1234, 100.0)
        e.balanceWith(1236)

        println(e)

        println(reporting.toString())

        println(reporting.generate())


    }

}