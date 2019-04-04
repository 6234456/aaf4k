package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.algorithm.Algorithm
import eu.qiou.aaf4k.algorithm.Algorithm.factorialPrime
import org.junit.Test

import org.junit.Assert.*

class AlgorithmTest {

    @Test
    fun gcd() {
        println(Algorithm.gcd(1160718174,316258250))
        println(Algorithm.gcd(12345,67890))
        println(Algorithm.gcd(54321,9876))
        println(Algorithm.gcd(0,9876))
        println(Algorithm.gcd(51,68))
        println(Algorithm.gcd(23,18))
        println(Algorithm.gcd(23,18))
        println(Algorithm.lcm(301337, 307829))
        println(Algorithm.gcd(301337, 307829))
    }

    @Test
    fun lcm(){
        val res = 18 * 720
        var cnt = 1
        var a = 0
        while (true){
            a = 18 * cnt ++
            if (a > res) break
            if (res.rem(a) != 0) continue

            val b = res / a
            if (Algorithm.gcd(a, b) == 18){
                println("$a , $b")
            }
        }
    }

    @Test
    fun serial(){
        val g: (Long) -> Long = { if(it.rem(2) == 0L) it/2 else (it * 3 + 1)}

        (1L until 100L).forEach {
            println( " ${String.format("%3d", it)}   =   ${Algorithm.serial(it, g)}" )
        }

    }

    @Test
    fun gcdS(){
       println( Algorithm.gcdSolution(155L, 341L))
       println( Algorithm.gcd(155L, 341L))
        // -2 + 11k    1 + 5k   1   -2 , 1   x, y
       println( Algorithm.gcdSolution(385L, 341L))
       println( Algorithm.gcd(385L, 341L))
        // 8 + 31j   -9 + 35k   0   0, 0    z,  y
       println( Algorithm.gcdSolution(155L, 385L))
       println( Algorithm.gcd(155L, 385L))
        // 5 + 77i    -2 + 31i   -6   -30, 12,  x,  z
        // -32  x   1  y   12 z
        // 155x + 341y + 385z
        //= 1.
       println( Algorithm.gcdSolution(37L, 47L))
    }

    @Test
    fun test1(){
        println(Algorithm.totalDigit(12345678))
        println(Algorithm.truncatNumber(12345, 1))
        println(Algorithm.digitAt(12345, 1))
        println(Algorithm.digitAt(123405, 2))
        println(Algorithm.threeDividable(111111111111))
        println((1 until 9 + 1).fold(0){ acc, i -> acc + Algorithm.digitAt(111111111, i)})
    }

    @Test
    fun test2(){
        Algorithm.primesBefore(1000030)
    }

    @Test
    fun congr(){
        println(Algorithm.congruence(8, 6, 14))
        println(Algorithm.congruence(72L, 47, 200))
        println(Algorithm.congruence(4183L, 5781, 15087))
    }

    @Test
    fun test3(){
        Algorithm.primesBefore(1000031)
        (1000031L downTo(1000000)).forEach {
            println(Algorithm.factorialToString(factorialPrime(it)))
        }
        println(factorialPrime(75460))
    }

    @Test
    fun phi(){
        println(Algorithm.factorialPrime(8800))
        println(Algorithm.euler_phi(8800))
    }
}