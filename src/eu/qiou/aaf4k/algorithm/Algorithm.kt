package eu.qiou.aaf4k.algorithm

import eu.qiou.aaf4k.util.mkString
import kotlin.math.roundToInt


object Algorithm {

    val primes = mutableListOf(2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L)

    fun sequentialIsPrime(n:Long):Boolean {
        if (primes.binarySearch(n) >= 0)
            return true

        if (preliminaryCheck(n)){
            val ende = Math.sqrt(n.toDouble()).roundToInt() + 1
            primes.forEachIndexed { index, l ->
                if (l > ende) {
                    primes.add(n)
                    return true
                }

                if (index > 2){
                    if(n.rem(l) == 0L)
                        return false
                }
            }
        }

        return false
    }

    private fun preliminaryCheck(n:Long):Boolean{
        val digit = truncatNumber(n, 1)
        if (digit.rem(2) == 0L || digit.rem(5) == 0L || threeDividable(n))
            return false

        return true
    }

    fun primesBefore(n: Long):List<Long>{
        if (primes.last() >= n) {
            var tmp = n
            while (true){
                val a = primes.binarySearch(tmp)
                if(a < 0) tmp-- else return primes.take(a+1)
            }
        }

        (primes.last() until(n+1)).forEach { sequentialIsPrime(it) }
        return primes
    }

    fun threeDividable(n:Long): Boolean {
        val len = Algorithm.totalDigit(n) + 1
        if (len < 3)  return n.rem(3L) == 0L

        return threeDividable((1 until len).fold(0){ acc, i -> acc + digitAt(n, i) }.toLong())
    }

    fun digitAt(number: Long, position: Int):Int {
        if( position <= 0 || position > totalDigit(number) )
            throw Exception("ParameterException: position $position not allowed")

       return (if(position == 1)
            truncatNumber(number, 1)
        else
            (truncatNumber(number, position) - truncatNumber(number, position -1)) / (1 until position-1).fold(10L){acc, _ -> acc * 10L }
               ).toInt()
    }

    fun truncatNumber(number: Long, take: Int):Long{
        val divisor = (1 until take).fold(10L){acc, _ -> acc * 10L }
        return number.rem(divisor)
    }

    fun totalDigit(number: Long): Int{
        var res = 1
        var divisor = 10L
        while (true){
            if (number.rem(divisor) == number)
                return res

            divisor *= 10L
            res++
        }
    }


    fun gcd(a: Long, b: Long):Long{
        if ( a < 0L || b < 0L)
            throw Exception("Only nature number supported.")

        if (a == 0L || b == 0L) return 0L

        if (a == b) return a

        return if (a > b){
            val rem2 = a.rem(b)

             if (rem2 == 0L)
                 b
            else
                 gcd(b, rem2)
        } else{
             gcd(b, a)
        }
    }

    fun gcd(a:Int, b:Int):Int {
        return gcd(a.toLong(), b.toLong()).toInt()
    }

    fun lcm(a: Int, b: Int):Long{
        return Algorithm.lcm(a.toLong(), b.toLong())
    }

    fun lcm(a:Long, b:Long):Long {
        return a * b / gcd(a, b)
    }

    fun serial(start: Long, generator: (Long)-> Long):List<Long>{
        val res = mutableListOf<Long>()
        var a = start

        while (!res.contains(a)){
            res.add(a)
            a = generator(a)
        }

        return res
    }

    fun gcdSolution(a: Long, b: Long, gcd: Long = gcd(a, b)): Pair<Long, Long>{
        if (a > b){
            if (gcd == 0L) return 0L to 0L

            if (b == gcd || a.rem(b) == gcd) return 1L to -1L * (a / b)

            val p = gcdSolution(b, a.rem(b), gcd)
            return p.second to (p.second * (a / b) * (-1L) + p.first)
        }

        return gcdSolution(b, a, gcd).let {
            it.second to it.first
        }
    }

    // invoke primeBefore to prepare the prime numbers first
    fun factorialPrime(n:Long): Map<Long, Int>{
        if (primes.binarySearch(n) >= 0){
            return mapOf(n to 1)
        }

        var tmp = n
        val res = mutableMapOf<Long, Int>()
        primes.forEach {
            if (tmp == 1L)
                return res

            var cnt = 0
            while (true){
                if(tmp.rem(it) != 0L){
                    if (cnt > 0){
                        res.put(it, cnt)
                    }
                    break
                }
                tmp /= it
                cnt++
            }
        }

        return res
    }

    fun factorialToString(map: Map<Long, Int>):String {
        return map.map { "${it.key}${if (it.value == 1) "" else "^${it.value}" }" }.mkString("*","","")
    }

    fun congruence(a: Long, c: Long, m: Long): List<Long> {
        val g = gcd(a, m)

        if (c.rem(g) != 0L) return listOf()

        val u0 = gcdSolution(a, m, g).first
        val x0 = u0 * (c/ g)

        val v = m / g

        return (0.until(g)).map { it * v + x0 }
    }

    fun euler_phi(n:Long):Long {
        return factorialPrime(n).keys.fold(n){acc, l -> acc / l * (l - 1L)  }
    }
}

