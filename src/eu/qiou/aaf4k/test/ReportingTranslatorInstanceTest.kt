package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.accounting.model.*
import eu.qiou.aaf4k.reportings.model.ProtoEntity
import eu.qiou.aaf4k.util.io.toReporting
import eu.qiou.aaf4k.util.template.Template
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

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

        val f = Reporting(234, "Demo2", "hgb", listOf(), entity = ProtoEntity(2, "Holding AG", "AG"))
                .cloneWith(e.structure)

        val p = ReportingPackage(e)
        p.localReportingOf(e)
        p.localReportingOf(f).apply {
            val c = this.categories[1] as Category
            Entry(c.nextEntryIndex, "trail", c).apply {
                this.add(1800, 23.4)
            }
        }

        p.toXl("data/packages.xls", t = Template.Theme.LAVENA)

    }
}