package eu.qiou.aaf4k.gui

import javax.script.ScriptEngineManager

// to fill in the tmpl reporting
/**
 * @param data which contains the key value pair, primarily the account id  to  account-value
 * @param prefix by default [
 * @param affix by default ]
 * @sample [1500] will be parsed as the value of the account
 *         [=[1005]+[1500]|%.2f]
 */
class TemplateEngine(data: Map<*, *>, val prefix: String = "[", val affix: String = "]") {
    private val regEvaluate = """\${prefix}=(.+)\s*\${affix}""".toRegex()
    //private val regEvaluate = """\${prefix}=(.+)(?:\s*\|\s*(%[\d\w.,]+))?\s*\${affix}""".toRegex()
    //private val regElement = """\${prefix}([^\${prefix}\${affix}]+)(?:\s*\|\s*(%[\d\w.,]+))?\s*\${affix}""".toRegex()
    private val regElement = """\${prefix}([^\${prefix}\${affix}]+)\${affix}""".toRegex()
    private val data = data.map { it.key.toString() to it.value.toString() }.toMap()
    private val js = ScriptEngineManager().getEngineByName("js")

    fun parse(tpl: String): String {
        if (regEvaluate.containsMatchIn(tpl)) {
            return regEvaluate.replace(tpl) { js.eval(parse(it.groups[1]!!.value)).toString() }
        }

        if (regElement.containsMatchIn(tpl)) {
            return parse(regElement.replace(tpl) {
                val v = it.groups[1]!!.value
                if (!data.containsKey(v))
                    throw Exception("the key [$v] is not provided")

                data[it.groups[1]!!.value]!!
            })
        }
        return tpl
    }

    fun containsTemplate(src: String): Boolean {
        return regElement.containsMatchIn(src)
    }
}