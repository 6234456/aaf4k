package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.strings.CollectionToString

object RegTest {
    @JvmStatic
    fun main(args: Array<String>) {
        /*val r = Regex("\\s+")
        val reg1 = Regex("""^\d+\s+""")
        println(r.split("123 wd 23", 2))
        println(reg1.containsMatchIn("1 s 1"))*/

        print(with(listOf<Int>(1,2,4)) { toString() })

    }
}