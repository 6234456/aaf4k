package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.base.*
import eu.qiou.aaf4k.util.io.toReporting
import eu.qiou.aaf4k.util.template.Template
import eu.qiou.aaf4k.util.time.TimeParameters
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class ReportingTranslatorInstanceTest {

    @Test
    fun translate() {
        val translator: ReportingTranslator = ReportingTranslatorInstance("data/cn/cn_translator_cas_hgb.txt")

        val p = translator.translate(mapOf(
                1001L to 100.0,
                100204L to 32.23
        ))

        println(p)
    }

    @Test
    fun packageToXl() {
        val e = Files.readAllLines(Paths.get("data/de_accounting.txt")).joinToString("\n").toReporting()
                .apply {
                    generate(true)
                }

        val f = Reporting(CollectionAccount(234, "Demo2",  entity = Entity(2, "Holding AG", "AG")).apply {
            addAll(e.structure)
        })

        val p = ReportingPackage(e)
        p.localReportingOf(e)
        p.localReportingOf(f).apply {
            val c = this.categories[1]
            Entry("trail", c).apply {
                this.add(1800, 23.4)
            }
        }

        p.toXl("data/packages.xls", t = Template.Theme.LAVANDA)

    }

    @Test
    fun testReportingPackage() {
        val e = AccountingFrame.inflate(0, "CAS", "data/cn/cn_cas1_2018.txt")

        val e3 = Entity(3, "C GmbH", "C")
        val e2 = Entity(2, "B GmbH", "B")
        val e1 = Entity(1, "A GmbH", "A").apply {
            add(e2)
            add(e3)
        }

        val r = e.toReporting(0, "GroupReportingOfAGmbH", TimeParameters.forYear(2018), entity = e1)

        val reg = """_vk_(.+)$""".toRegex()

        Locale.setDefault(Locale.GERMAN)

        val rePackage = ReportingPackage(r) { e, a ->
            if (reg.containsMatchIn(a.name)) {
                val ent = e1.findChildBy({ x -> x.abbreviation == reg.find(a.name)!!.groups[1]!!.value })!!
                InterCompanyPolicy(e, ent, a as Account)

            } else {
                null
            }
        }
        val rePackage1 = ReportingPackage(r) { e, a ->
            if (reg.containsMatchIn(a.name)) {
                val ent = e1.findChildBy({ x -> x.abbreviation == reg.find(a.name)!!.groups[1]!!.value })!!
                InterCompanyPolicy(e, ent, a as Account)

            } else {
                null
            }
        }

        rePackage.localReportingOf(
            (r.update(
                mapOf(
                        112203L to 2345.65,
                        112204L to 2345.65
                )
            ) as Reporting
        ))

        rePackage.localReportingOf(e.toReporting(1, "B GmbH", entity = e2).update(
                mapOf(
                        112202L to -2345.65,
                        112204L to 2345.65
                )
        ) as Reporting)
        rePackage.localReportingOf(e.toReporting(2, "C GmbH", entity = e3).update(
                mapOf(
                        112203L to -2345.65,
                        112202L to -2348.65
                )
        ) as Reporting)

        rePackage.eliminateIntercompanyTransactions()

        rePackage.carryForward(rePackage1)

      rePackage.toXl("data/package2.xlsx")
    }


    @Test
    fun trail() {
        val reg = """_vk_C""".toRegex()
        val s = "对集团内公司应收_vk_C"

        println(reg.containsMatchIn(s))
    }
}

