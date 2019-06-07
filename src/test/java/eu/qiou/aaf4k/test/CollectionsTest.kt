package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.rotate
import eu.qiou.aaf4k.util.sample
import org.junit.Test

class CollectionsTest {

    @Test
    fun permutation() {
        val l0 = listOf<Int>()
        val l1 = listOf<Int>(1, 2, 3, 4, 5)

        println(l1.rotate(4))
        println(l1.rotate(1))
        println(l1.sample(3))
    }

    @Test
    fun trail() {
        println(twoSum(arrayOf(1, 2, 3, 5, 9, 10, 8).toIntArray(), 17).toList())
    }


    fun twoSum(nums: IntArray, target: Int): IntArray {
        nums.forEachIndexed { index, i ->
            ((index + 1) until nums.size).forEach { j ->
                if (nums[j] + i == target)
                    return arrayOf(index, j).toIntArray()
            }
        }

        return IntArray(0)
    }
}