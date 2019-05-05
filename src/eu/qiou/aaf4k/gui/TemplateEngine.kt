package eu.qiou.aaf4k.gui

import java.util.*
import javax.script.ScriptEngineManager

// to fill in the tmpl reporting
/**
 * @param prefix by default [
 * @param affix by default ]
 * @param fmt default number format, the format of the value can be specified separately
 * @sample [1500] will be parsed as the value of the account
 *         [=[1005]+[1500]|%.2f]
 *         [= new Date()]
 */
class TemplateEngine(val prefix: String = "[", val affix: String = "]", val fmt: String = "%,.2f") {
    private val regEvaluate = """\$prefix=([^|]+)(?:\s*\|\s*(%[\d\w.,]+))?\s*\$affix""".toRegex()
    private val regElement = """\$prefix([^\$prefix\$affix\|]+)(?:\s*\|\s*(%[\d\w.,]+))?\s*\$affix""".toRegex()
    private val js = ScriptEngineManager().getEngineByName("js")

    private fun parse(tpl: String, data: Map<String, Any>, withBrackets: Boolean = false): String {
        var fmt = fmt

        if (regEvaluate.containsMatchIn(tpl)) {
            regEvaluate.find(tpl)!!.groups[2]?.let {
                fmt = it.value
            }

            return regEvaluate.replace(tpl) {
                val v = js.eval(parse(it.groups[1]!!.value, data, true))
                if (v is Number)
                    if (v.toDouble() < 0 && withBrackets) "(${v.toDouble()})" else String.format(fmt, v.toDouble())
                else
                    String.format("%s", v)

            }
        }

        if (regElement.containsMatchIn(tpl)) {
            return parse(regElement.replace(tpl) {
                val v = it.groups[1]!!.value.trim()
                if (!data.containsKey(v))
                    throw Exception("the key [$v] is not provided")

                regElement.find(tpl)!!.groups[2]?.let {
                    fmt = it.value
                }

                val value = data[v]!!
                if (value is Number)
                    if (value.toDouble() < 0 && withBrackets) "(${value.toDouble()})" else String.format(fmt, value.toDouble())
                else
                    String.format("%s", value)

            }, data)
        }
        return tpl
    }


    fun compile(tpl: String): (Map<String, Any>) -> String {
        val s = Stack<Int>()
        val rng: MutableList<IntRange> = mutableListOf()

        0.until(tpl.length).forEach {
            when (tpl[it].toString()) {
                prefix -> s.push(it)
                affix -> {
                    if (s.size == 1)
                        rng.add(s.pop().until(it + 1))
                    else if (s.isNotEmpty())
                        s.pop()
                }
            }
        }

        return {
            replaceRng(tpl, rng.map { x -> x to parse(tpl.substring(x), it) }.toMap())
        }
    }

    companion object {
        //m is sorted not overlapping ranges
        fun replaceRng(tpl: String, m: Map<IntRange, String>): String {
            val l = m.toList()
            if (m.isEmpty())
                return tpl

            return l.foldIndexed("") { i, acc, pair ->
                acc +
                        (
                                if (i == 0) if (pair.first.first == 0) "" else tpl.substring(0, pair.first.first)
                                else tpl.substring(l[i - 1].first.last + 1, pair.first.first)
                                ) +
                        pair.second + (
                        if (i == l.size - 1) tpl.substring(pair.first.last + 1)
                        else "")
            }
        }
    }

    fun containsTemplate(src: String): Boolean {
        return regElement.containsMatchIn(src)
    }
}