package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.accounting.model.*
import eu.qiou.aaf4k.gui.GUI
import eu.qiou.aaf4k.reportings.etl.AccountingFrame
import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.util.groupNearby
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.UnitScalar
import org.junit.Test

class AccountingFrameTest {

    companion object {
        fun testReporting(): Reporting {
            val frame = AccountingFrame.inflate(123, "cn_cas_2018").toReporting(123, "ED",
                    displayUnit = CurrencyUnit(UnitScalar.UNIT, "EUR"), timeParameters = TimeParameters.forYear(2016))
            val category = Category("年度账户", 0, "laufende Buchungen", frame)
            val category1 = Category("合并抵销分录", 1, "laufende Buchungen", frame)
            val category2 = Category("权益抵销分录", 2, "laufende Buchungen", frame)
            val entry = Entry(0, "Demo1", category)

            entry.add(3100, 3000.0)
            entry.add(3400, 3400.0)
            entry.balanceWith(3200)

            val entry1 = Entry(1, "Demo2", category)

            entry1.add(3100, 3000.0)
            entry1.add(3400, 3400.0)
            entry1.balanceWith(3200)

            Entry(2, "Trail", category1).apply {
                add(1005, 3000.0)
                add(2900, 3400.0)
                balanceWith(3200)
            }

            Entry(2, "Demo3", category2).apply {
                add(1005, 3000.0)
                add(2800, 3400.0)
                balanceWith(3200)
            }

            return frame
        }
    }

    @Test
    fun getFlattened() {

        testReporting().apply {
            println(this)
            toXl("data/demo.xlsx")
        }

        GUI.open()

    }

    @Test
    fun groupNearBy() {
        println(listOf(1, 3, 3, 3, 4, 4, 5, 6, 1, 1, 3).groupNearby { i -> i })
        println(listOf(1, 3, 3, 3, 4, 4, 5, 6, 1, 1).groupNearby { i -> i * 0.1 })
    }

    @Test
    fun findById() {
        val acc1 = Account.from(ProtoAccount.Builder(123, "Demo").setValue(10.0).build(), ReportingType.LIABILITY)
        val acc2 = Account.from(ProtoAccount.Builder(124, "Demo1").setValue(10).build(), ReportingType.LIABILITY)
        val acc3 = Account.from(ProtoAccount.Builder(125, "Demo2").setValue(10).build(), ReportingType.LIABILITY)
        val acc4 = Account.from(ProtoAccount.Builder(126, "Demo2").setValue(mutableSetOf(acc1, acc2) as MutableSet<ProtoAccount>).build(), ReportingType.LIABILITY)

        val acc = Account.from(ProtoAccount(1, "Hi", subAccounts = mutableSetOf(acc3, acc4)), ReportingType.LIABILITY)

        println(acc.subAccounts)

    }
}