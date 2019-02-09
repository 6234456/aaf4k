package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.gui.ExcelReportingTemplate
import eu.qiou.aaf4k.gui.TemplateEngine
import eu.qiou.aaf4k.util.io.toReporting
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class TemplateEngineTest {


    @Test
    fun export() {
        val t = ExcelReportingTemplate("data/demo.xls")
        t.export(mapOf(1 to 0, 2 to 3, 3 to 0, "公司" to "示例公司", "年份" to 2018), "data/exp.xls", { true })
    }

    @Test
    fun export1() {
        val t = ExcelReportingTemplate("data/demo.xls", shtName = "tpl")
        t.export(mapOf(1 to 0, 2 to 3, 3 to 0, "公司" to "示例公司", "年份" to 2018, "1500" to "hi",1500 to "Test"), "data/exp2.xls")
    }

    @Test
    fun fillReporting() {
        val r = Files.readAllLines(Paths.get("data/accounting.txt")).joinToString("").toReporting()
                .generate()
        val data = r.flattened.map { it.id to it.decimalValue }.toMap() +
                mapOf("E" to r.entity.abbreviation, "Y" to r.timeParameters.end.year, "M" to r.timeParameters.end.monthValue, "D" to r.timeParameters.end.dayOfMonth)

        ExcelReportingTemplate("data/demo.xls").export(data, "data/exp.xls")
    }

    @Test
    fun trail() {
        println(String.format("%,.3f", 12300.0))
        println(TemplateEngine.replaceRng("sss", mapOf(1.until(2) to "1", 2.until(3) to "ss")))
        val s: Any = 3.0
        println(s is Number)
        val t = TemplateEngine(fmt = "%.0f")
        val d = mapOf("1234" to 2.0, "2345" to 5.0, "2" to 7.0, "9" to -1121, "10" to -12222)

        println(t.compile("abc[=[1234] * [2345]]++ [= 1+ 2]")(d))
        println(t.compile("abc[=[1234] * [2345] | %.0f ]++[= [2345]]")(d))
        println(t.compile("abc[[1234] | %.6f ] [=[1234] | %.2f ] sersdf8[")(d))
        println(t.compile("abc[=[10] | %,.2f]")(d))
        println(t.compile("abc[=[9]-[10] | %,.2f]")(d))
        println(t.compile("abc[10 | %,.2f] | %,.2f]")(d))
    }
}