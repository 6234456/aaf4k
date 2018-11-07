package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.gui.ExcelReportingTemplate
import eu.qiou.aaf4k.gui.TemplateEngine
import org.junit.Test

class TemplateEngineTest {


    @Test
    fun export() {
        val t = ExcelReportingTemplate("data/demo.xlsx")
        t.export(mapOf(1 to 0, 2 to 3, 3 to 0, "公司" to "示例公司", "年份" to 2018), "data/exp.xlsx", { true })
    }

    @Test
    fun trail() {
        println(String.format("%,.3f", 12300.0))
        println(TemplateEngine.replaceRng("sss", mapOf(1.until(2) to "1", 2.until(3) to "ss")))
        val s: Any = 3.0
        println(s is Number)
        val t = TemplateEngine(fmt = "%.0f")
        val d = mapOf("1234" to 2.0, "2345" to 5.0, "2" to 7.0)

        println(t.compile("abc[=[1234] * [2345]]++ [= 1+ 2]")(d))
        println(t.compile("abc[=[1234] * [2345] | %.0f ]++[= [2345]]")(d))
        println(t.compile("abc[[1234] | %.6f ] [=[1234] | %.2f ] sersdf8[")(d))
    }
}