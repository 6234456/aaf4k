package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.algorithm.Algorithm
import eu.qiou.aaf4k.util.algorithm.Algorithm.euler_phi
import eu.qiou.aaf4k.util.algorithm.Algorithm.factorialPrime
import eu.qiou.aaf4k.util.algorithm.Algorithm.multicongruence
import eu.qiou.aaf4k.util.algorithm.crypto.RSA
import org.junit.Assert
import org.junit.Test
import java.math.BigInteger

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
        println(Algorithm.euler_phi(1750))
        println(Algorithm.factorialPrime(1750))

        println((161 until (10000L)).filter { euler_phi(it) == 160L })
        println((1001 until (10000L)).filter { euler_phi(it) == 1000L })
        println((1001 until (10000L)).filter { euler_phi(it) == 1000L }.map { Algorithm.factorialPrime(it) })
    }

    @Test
    fun gcdss(){
        println(multicongruence(3, 7 ,5, 9))

        println(multicongruence(3, 7 ,5, 9))
        println(multicongruence(3, 37 ,1, 87))

        //886
        println(multicongruence(5, 7 ,2, 12, end = 7L * 12L * 13L))
        println(multicongruence(5, 7 ,8, 13, end = 7L * 12L * 13L))
        println(multicongruence(2, 12 ,8, 13, end = 7L * 12L * 13L))

        //
        println(multicongruence(2, 3 ,3, 5, end = 7L * 3L * 5L))
        println(multicongruence(2, 3 ,2, 7, end = 7L * 3L * 5L))
        println(multicongruence(3, 5 ,2, 7, end = 7L * 3L * 5L))
    }

    @Test
    fun sdf(){
        println((1 until (10000L)).filter { euler_phi(it) == (it/2) })
        println((1 until (10000L)).filter { euler_phi(it) == (it/3) })
        println((1 until (10000L)).filter { euler_phi(it) == (it/6) })
        println(euler_phi(58))
        println(Algorithm.factorialPrime(58))
        println(Algorithm.factorialPrime(324))
    }

    @Test
    fun sig(){
        println(Algorithm.sigma(10))
        println(Algorithm.sigma(20))
        println(Algorithm.sigma(1728))
        println(Algorithm.sigma(172800))

        println(Algorithm.socialables(14316, 28))
        println(Algorithm.socialables(1547860, 9))

        println(euler_phi(12830603))
    }

    @Test
    fun socialable(){
        for (i in 800000000L..850000000L){
            if (i.rem(1000L) == 0L)
                println(i)

            // order 4: [1547860, 1305184, 1727636, 1264460]
            Algorithm.socialables(i, 9)?.let {
                if (it.size >= 3)
                    println("$i : $it")
            }
        }
    }

    @Test
    fun p(){
        println(Algorithm.modPow(5, 13, 23))
        println(Algorithm.modPow(7, 327, 853))
        println(Algorithm.modPow(7, 7386, 7387))
        println(Algorithm.modPow(37, 113, 463))

        println(Algorithm.checkLargePrime(8500000003))
        println(Algorithm.checkLargePrime(9991))
    }

    @Test
    fun rootsMood(){
        println(Algorithm.solveRootsMod(131, 758, 1073))
        println(Algorithm.solveRootsMod(329, 452, 1147))
        println(Algorithm.solveRootsMod(113, 347, 463))
        println(Algorithm.euler_phi(1073))
        println(Algorithm.euler_phi(463))
        println(Algorithm.gcdSolution(131, 1008))
        println(Algorithm.gcdSolution(113, 462))
    }

    @Test
    fun num(){
        println(Algorithm.splitNumberAt(1, 1))
        println(Algorithm.splitNumberAt(10, 1))
        Assert.assertEquals(Algorithm.truncatNumber(12232123, 1), 3L)
        Assert.assertEquals(Algorithm.truncatNumber(12232123, 2), 23L)
        Assert.assertEquals(Algorithm.truncatNumber(12232123, 2, true), 12L)
        Assert.assertEquals(Algorithm.splitNumberAt(12232123, 2), 122321L to 23L)
        println(Algorithm.splitNumberAt(12232123, 0, true))
        println(Algorithm.splitNumberAt(122, 3, true))
        println(Algorithm.splitNumberAt(122, 0, true))
        println(Algorithm.splitNumberAt(122, 0))
        println(Algorithm.splitNumberAt(122, 1))
        println(Algorithm.splitNumberAt(122, 2))
        println(Algorithm.splitNumberAt(122, 3))
        println(Algorithm.totalDigit(0))
    }

    @Test
    fun RSATest(){
        val a = RSA(12553,13007,79921)
        val msg = "Qiou Is Great ddddddddd"
        println(msg.toCharArray().map { it.toInt() })
        println(a.decode(a.encode(msg)))
        println(a.decode(a.encode(msg)).toCharArray().map { it.toInt() })
    }

    @Test
    fun pol(){
        println(Algorithm.pollars_rho(12553))
        println(Algorithm.pollars_rho(BigInteger("12553") * BigInteger("13007")))
        println(Algorithm.pollars_rho(BigInteger("47386483629775753")))
    }
}