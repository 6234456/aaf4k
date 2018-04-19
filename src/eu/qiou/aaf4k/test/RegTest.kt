package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.mergeReduce

object RegTest {
    @JvmStatic
    fun main(args: Array<String>) {

        val m1: Map<Int, Double> = mapOf(1 to 2.0, 2 to 3.0, 0 to 1.0)
        val m2: Map<Int, Double> = mapOf(1 to 2.0, 2 to 3.0, 4 to 1.0)

        println(m1.mergeReduce(m2, { a, b -> a + b }))

    }
}