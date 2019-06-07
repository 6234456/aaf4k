package eu.qiou.aaf4k.util

import org.junit.Test

class CollectionsKtTest {

    @Test
    fun permutationDistinct() {
    }

    @Test
    fun permutation() {
        println(listOf(1, 1, 2, 2, 3, 3, 2).permutation())
    }

    @Test
    fun insert() {
        // 6s are all in the distinct position  of 4   -> C43
        // 6s are in the same position 4
        // 2 6s in one and 1 6 in another ->  A42
        println(listOf<Int>(1, 2, 3).insert(6, 3).size == 4 + 4 + 4 * 3)

    }
}