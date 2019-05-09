package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.SortedList
import org.junit.Test

class SortedListTest {

    @Test
    fun add() {
        val l: SortedList<Int> = SortedList()
        println(l.apply { addAll(listOf(10, 5, 3, 10, 2, 1, 8, 0, 5, 6, 4, 7)) })

        println(l.mergeWith(l))
        l.mergeWithInPlace(l)

        println(l)
    }
}