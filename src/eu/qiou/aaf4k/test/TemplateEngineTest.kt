package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.gui.ExcelReportingTemplate
import eu.qiou.aaf4k.gui.TemplateEngine
import org.junit.Test

class TemplateEngineTest {

    @Test
    fun parse() {
        val t = TemplateEngine(mapOf(1234 to 2, 2345 to 5, 2 to 7))

        println(t.parse("abc[1234]+[2345]"))
        println(t.parse("abc[1234"))
        println(t.parse("abc[1234]]"))
        println(t.parse("abc[=1+2]++"))
        println(t.parse("abc[= 1>2?3:4 ]++"))
        println(t.parse("abc[= [1234] > 0 ]++"))
        println(t.parse("abc[[1234]]"))
        //println(t.parse("abc[[1234]] [=[1234]]"))
        println(t.parse("abc[=[1234] * [2345]]++"))
        //println(t.parse("abc[=[1234] * [2345]]++[= [2345]]"))

        val t1 = TemplateEngine(mapOf(1234 to 2, 2345 to 5, 2 to 7), "{", "}")

        println(t1.parse("abc{1234}+[2345]"))


        /*println(t.parse("€ [1234]"))
        println(t.parse("[[1234]+[2345]]"))*/
    }

    @Test
    fun export() {
        val t = ExcelReportingTemplate("data/demo.xls")
        t.export(mapOf(1 to 0, 2 to 3), "data/exp.xls")
    }

    @Test
    fun trail() {
        println(String.format("%,.3f", 12300.0))
    }
}