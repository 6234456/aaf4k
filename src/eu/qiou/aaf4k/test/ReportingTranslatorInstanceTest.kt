package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.accounting.model.ReportingPackage
import eu.qiou.aaf4k.accounting.model.ReportingTranslator
import eu.qiou.aaf4k.accounting.model.ReportingTranslatorInstance
import eu.qiou.aaf4k.util.io.toReporting
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
        e.clearCategories()

        val p = ReportingPackage(e)
        p.localReportingOf(e)

        p.toXl("data/packages.xls")

    }
}