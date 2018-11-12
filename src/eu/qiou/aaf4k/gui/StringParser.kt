package eu.qiou.aaf4k.gui

import javax.script.ScriptEngineManager

object StringParser {
    val regBinding = """^=([\$\(\)\.\+\-\*/\d]*)\s*$""".toRegex()
    val regBindingElement = """\$(\d+)""".toRegex()
    private val js = ScriptEngineManager().getEngineByName("js")

    // bindingString starts with $()
    // $1 position of the target element in the srcList
    fun <T> parseBindingString(bindingString: String, f: T.() -> Double, list: List<T>): (() -> Double) {
        if (!regBinding.matches(bindingString))
            throw Exception("IllegalBindingString: $bindingString ")

        val content = regBinding.find(bindingString)!!.groups[1]!!.value

        return {
            js.eval(regBindingElement.replace(content) {
                list[it.groups[1]!!.value.toInt()].f().toString()
            }).toString().toDouble()
        }
    }
}