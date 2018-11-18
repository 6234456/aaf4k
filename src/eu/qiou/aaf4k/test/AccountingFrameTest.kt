package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.accounting.model.*
import eu.qiou.aaf4k.gui.GUI
import eu.qiou.aaf4k.reportings.etl.AccountingFrame
import eu.qiou.aaf4k.reportings.model.Address
import eu.qiou.aaf4k.reportings.model.Person
import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.reportings.model.ProtoEntity
import eu.qiou.aaf4k.util.groupNearby
import eu.qiou.aaf4k.util.io.*
import eu.qiou.aaf4k.util.template.Template
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.UnitScalar
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class AccountingFrameTest {
    @Test
    fun trailDE() {
        val frame: Reporting = AccountingFrame.inflate(123, "de_hgb_2018").toReporting(123, "ED",
                displayUnit = CurrencyUnit(UnitScalar.UNIT, "EUR"), timeParameters = TimeParameters.forYear(2016)).update(
                mapOf(24 to 25000.0, 61 to -25000.0)
        )

        val cat1 = Category("KapKons-Erst", 1, "Kapital Konsolidierung", frame)
        val cat4 = Category("KapKons-Folge", 2, "Kapital Konsolidierung", frame)
        val cat5 = Category("ZG-Elimilierung", 3, "Kapital Konsolidierung", frame)
        val cat2 = Category("SchuKons", 4, "Kapital Konsolidierung", frame)
        val cat3 = Category("A/E-Kons", 5, "Kapital Konsolidierung", frame)

        Paths.get("data/de_trail.xlsx").toFile().let {
            if (it.exists())
                it.delete()
        }
        frame.toXl("data/de_trail.xlsx", t = Template.Theme.LAVENA)

        Files.write(Paths.get("data/de_accounting.txt"), frame.toJSON().lines())
    }

    @Test
    fun trailDE1() {
        val frame: Reporting = AccountingFrame.inflate(123, "hgb", "credentials/de_hgb_2018.txt").toReporting(123, "ED",
                displayUnit = CurrencyUnit(UnitScalar.UNIT, "EUR"), timeParameters = TimeParameters.forYear(2016))
        val cat1 = Category("KapKons-Erst", 1, "Kapital Konsolidierung", frame)
        val cat4 = Category("KapKons-Folge", 2, "Kapital Konsolidierung", frame)
        val cat5 = Category("ZG-Elimilierung", 3, "Kapital Konsolidierung", frame)
        val cat2 = Category("SchuKons", 4, "Kapital Konsolidierung", frame)
        val cat3 = Category("A/E-Kons", 5, "Kapital Konsolidierung", frame)

        Paths.get("data/de_trail.xlsx").toFile().let {
            if (it.exists())
                it.delete()
        }
        frame.toXl("data/de_trail.xlsx", t = Template.Theme.LAVENA)

        Files.write(Paths.get("data/de_accounting.txt"), frame.toJSON().lines())
    }


    companion object {
        fun testReporting(): Reporting {
            val frame = AccountingFrame.inflate(123, "cn_cas_2018").toReporting(123, "ED",
                    displayUnit = CurrencyUnit(UnitScalar.UNIT, "EUR"), timeParameters = TimeParameters.forYear(2016)).update(
                    mapOf(3100 to 1203.0, 3400 to -1203.0)
            )

            val category = Category("年度账户", 0, "laufende Buchungen", frame)
            val category1 = Category("合并抵销分录", 1, "laufende Buchungen", frame)
            val category2 = Category("权益抵销分录", 2, "laufende Buchungen", frame)
            val entry = Entry(0, "Demo1", category)

            entry.add(3100, 3000.0)
            entry.add(3400, 3400.0)
            entry.balanceWith(3200)

            val entry1 = Entry(category.nextEntryIndex, "Demo2", category)

            entry1.add(3100, 3000.0)
            entry1.add(3400, 3400.0)
            entry1.balanceWith(3200)

            Entry(category1.nextEntryIndex, "Trail", category1).apply {
                add(1005, 3000.0)
                add(2900, 3400.0)
                balanceWith(3200)
            }

            Entry(category2.nextEntryIndex, "Demo3", category2).apply {
                add(1005, 3000.0)
                add(2800, 3400.0)
                balanceWith(3200)
            }

            category.summarizeResult()
            category1.summarizeResult()
            category2.summarizeResult()

            return frame
        }
    }

    @Test
    fun stat() {
        val reporting = testReporting()
        Entry(8, "dsder", reporting.categories.elementAt(0) as Category).add(
                5202, 100.0
        )

        reporting.findAccountByID(5202)!!.let {
            println(it.isStatistical)
            println(it.toBuilder().isStatistical)
            println(it.toBuilder()
                    .setValue(v = 1000.0, decimalPrecision = it.decimalPrecision)
                    .build().isStatistical)
            println(Account.from(it.toBuilder()
                    .setValue(v = 1000.0, decimalPrecision = it.decimalPrecision)
                    .build(), it.reportingType).isStatistical)
        }

    }

    @Test
    fun getFlattened() {

        testReporting().apply {
            toXl("data/demo.xlsx")
        }
    }

    @Test
    fun groupNearBy() {
        println(listOf(1, 3, 3, 3, 4, 4, 5, 6, 1, 1, 3).groupNearby { i -> i })
        println(listOf(1, 3, 3, 3, 4, 4, 5, 6, 1, 1).groupNearby { i -> i * 0.1 })
    }


    @Test
    fun nullify() {
        println(Account.from(ProtoAccount.Builder(123, "Demo").setValue(10.0).build(), ReportingType.LIABILITY).nullify())
        Files.write(Paths.get("data/accounting.txt"), AccountingFrameTest.testReporting().toJSON().lines())
    }

    @Test
    fun parseJSON() {
        val s = AccountingFrameTest.testReporting()

        print(s.categories.elementAt(0).toJSON())
    }

    @Test
    fun parseJSONTimeParameter() {
        val s = TimeParameters.forYear(2018).toJSON()

        println(s)
        println(FromJSON.timeParameters(FromJSON.read(s)))

        val s1 = TimeParameters(null, null).toJSON()

        println(s1)
        println(FromJSON.timeParameters(FromJSON.read(s1)))

        val s2 = TimeParameters(2017, 10, 11).toJSON()

        println(s2)
        println(FromJSON.timeParameters(FromJSON.read(s2)))

        val p = Person(123, "Yang", "Qiou", true)
        println(p.toJSON())
        println(p.toJSON().toPerson())

        val a = Address(123, Locale.CHINA, "Hubei", "Qianjiang", "433100", "myStreet", "123a")
        println(a)
        println(a.toJSON().toAddress())

        val entity = ProtoEntity(1234, "Qiou GmbH", "Qiou", "dsf", p, a)
        println(entity)
        println(entity.toJSON().toEntity())

        val re = AccountingFrameTest.testReporting()
        println(re.toJSON().toReporting().entity.toJSON())

    }

    @Test
    fun trailAddAccount() {
        val r = Files.readAllLines(Paths.get("data/de_accounting.txt")).joinToString("\n").toReporting()
        //println(r.addAccountTo(Account.from(ProtoAccount(0, "hi", 123L,2), ReportingType.ASSET_SHORT_TERM), 0))
        /*GUI.open((r.addAccountTo(Account.from(ProtoAccount(0, "hi", 123L,2), ReportingType.ASSET_SHORT_TERM), 0, 10000)
                .addAccountTo(Account.from(ProtoAccount(-2, "hi", 123L,2), ReportingType.ASSET_SHORT_TERM),0, 110000) as Reporting)
        )*/
        GUI.open(r.removeAccount(10000).apply { addConsolidationCategories(Locale.CHINESE) })
    }
}