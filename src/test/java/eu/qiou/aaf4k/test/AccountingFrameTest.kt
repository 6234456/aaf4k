package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.gui.GUI
import eu.qiou.aaf4k.reportings.base.*
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
        val frame: Reporting = AccountingFrame.inflate(0, "de_hgb_2018")
                .toReporting(-1, "ED", displayUnit = CurrencyUnit(UnitScalar.UNIT, "EUR"),
                        timeParameters = TimeParameters.forYear(2016)).apply {
                    update(mapOf(111L to 2123.23, 124L to 20.0 ))
                }

        println("start assert")

        assert(frame.countRecursively(false) == frame.sortedList().size)
        assert(frame.countRecursively(true) == frame.sortedAllList().size)
        assert(frame.search(7) != null)
        assert(frame.search(6) != null)
        assert(frame.findAccountByID(6) != null)

        println("assert ends")
        with(frame.search(6)) {

            this as CollectionAccount

            println(countRecursively(true))
            println(sortedAllList().map { it.id })
            assert(countRecursively(true) == sortedAllList().size)

            search(63).let {
                it as CollectionAccount

                assert(it.countRecursively(false) == 4)
                it.add(this.search(65)!!.deepCopy())
                println(it)
            }

            println(search(6))
        }

        println("end compare")

        frame.addAccountTo((frame.search(634)!! as Account).copy(isStatistical = true), 0, 7)
        println(frame.search(7))



        Category("KapKons-Erst", "Kapital Konsolidierung", frame)
        Category("KapKons-Folge",  "Kapital Konsolidierung", frame)
        Category("ZG-Elimilierung",  "Kapital Konsolidierung", frame)
        Category("SchuKons",  "Kapital Konsolidierung", frame)
        Category("A/E-Kons", "Kapital Konsolidierung", frame)

        Paths.get("data/de_trail.xlsx").toFile().let {
            if (it.exists())
                it.delete()
        }
        frame.toXl("data/de_trail.xlsx", t = Template.Theme.LAVANDA)

        Files.write(Paths.get("data/de_accounting.txt"), frame.toJSON().lines())
    }

    @Test
    fun trailDE1() {
        val frame: Reporting = AccountingFrame.inflate(123, "hgb", "credentials/de_hgb_2018.txt").toReporting(123, "ED",
                displayUnit = CurrencyUnit(UnitScalar.UNIT, "EUR"), timeParameters = TimeParameters.forYear(2016))
        Category("KapKons-Erst", "Kapital Konsolidierung", frame)
        Category("KapKons-Folge", "Kapital Konsolidierung", frame)
        Category("ZG-Elimilierung", "Kapital Konsolidierung", frame)
        Category("SchuKons", "Kapital Konsolidierung", frame)
        Category("A/E-Kons", "Kapital Konsolidierung", frame)

        Paths.get("data/de_trail.xlsx").toFile().let {
            if (it.exists())
                it.delete()
        }
        frame.toXl("data/de_trail.xlsx", t = Template.Theme.LAVANDA)

        Files.write(Paths.get("data/de_accounting.txt"), frame.toJSON().lines())
    }


    companion object {
        fun testReporting(): Reporting {
            val frame = AccountingFrame.inflate(123, "cn_cas_2018").toReporting(123, "ED",
                    displayUnit = CurrencyUnit(UnitScalar.UNIT, "EUR"), timeParameters = TimeParameters.forYear(2016)).apply {
                update(
                        mapOf(3100L to 1203.0, 3400L to -1203.0)
                )
            }


            val category = Category("年度账户", "laufende Buchungen", frame)
            val category1 = Category("合并抵销分录", "laufende Buchungen", frame)
            val category2 = Category("权益抵销分录", "laufende Buchungen", frame)
            val entry = Entry("Demo1", category)

            entry.add(3100, 3000.0)
            entry.add(3400, 3400.0)
            entry.balanceWith(3200)

            val entry1 = Entry("Demo2", category)

            entry1.add(3100, 3000.0)
            entry1.add(3400, 3400.0)
            entry1.balanceWith(3200)

            Entry("Trail", category1).apply {
                add(1005, 3000.0)
                add(2900, 3400.0)
                balanceWith(3200)
            }

            Entry("Demo3", category2).apply {
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
        Entry("dsder", reporting.categories.elementAt(0)).add(
                5202, 100.0
        )

        reporting.findAccountByID(5202)!!.let {
            println(it.isStatistical)
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
        println(Account(123,"Demo",34234, reportingType = ReportingType.LIABILITY).nullify())
        Files.write(Paths.get("data/accounting.txt"), testReporting().toJSON().lines())
    }

    @Test
    fun parseJSON() {
        val s = testReporting()

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

        val entity = Entity(1234, "Qiou GmbH", "Qiou", "dsf", p, a)
        println(entity)
        println(entity.toJSON().toEntity())

        val re = testReporting()
        println(re.toJSON().toReporting().entity.toJSON())

    }

    @Test
    fun carryForward() {
        val r = AccountingFrame.inflate(123, "hgb", "data/de/credentials.de_hgb_2018.txt").toReporting(123, "Demo", entity = Entity(123, "Qiou GmbH", "Qiou"))
        r.prepareConsolidation(Locale.ENGLISH)
        Files.write(Paths.get("data/de_accounting.txt"), r.toJSON().split("\n"))
    }

    @Test
    fun carryForward2() {
        GUI.locale = Locale.ENGLISH
        GUI.open("data/de_accounting.txt")
    }

    @Test
    fun carryForward3() {
        val r = Files.readAllLines(Paths.get("data/de_accounting.txt")).joinToString { it }.toReporting()
        println(r.categories.elementAt(0).entries.elementAt(0))
    }
}