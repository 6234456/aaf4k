package eu.qiou.aaf4k.util.algorithm

import eu.qiou.aaf4k.util.mkString
import java.math.BigInteger
import kotlin.math.roundToInt
import kotlin.random.Random


object Algorithm {

    val PRIMES = mutableListOf(2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L)
    private val CHECK_IF_GT = 100_000L

    fun sequentialIsPrime(n:Long):Boolean {
        if (PRIMES.binarySearch(n) >= 0)
            return true

        if (preliminaryCheck(n) || (n >= CHECK_IF_GT && checkLargePrime(n))){
            val ende = Math.sqrt(n.toDouble()).roundToInt() + 1
            PRIMES.forEachIndexed { index, l ->
                if (l > ende) {
                    PRIMES.add(n)
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
        if (PRIMES.last() >= n) {
            var tmp = n
            while (true){
                val a = PRIMES.binarySearch(tmp)
                if(a < 0) tmp-- else return PRIMES.take(a+1)
            }
        }

        (PRIMES.last() until(n+1)).forEach { sequentialIsPrime(it) }
        return PRIMES
    }

    fun threeDividable(n:Long): Boolean {
        val len = totalDigit(n) + 1
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

    // take the digit from right to left, starting from 1
    fun truncatNumber(number: Long, take: Int, fromLeft: Boolean = false):Long{
        val tmp = splitNumberAt(number, take, fromLeft)
        return if (fromLeft) tmp.first else tmp.second
    }

    fun splitNumberAt(number: Long, at: Int, fromLeft: Boolean = false): Pair<Long, Long>{
        if (fromLeft && totalDigit(number) == at)
            return number to 0L

        if (!fromLeft && at == 0)
            return number to 0L

        val divisor = (1 until if (fromLeft) totalDigit(number) - at else at).fold(10L){acc, _ -> acc * 10L }
        return  (number / divisor) to number.rem(divisor)
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

    fun gcd(a: BigInteger, b: BigInteger): BigInteger {
        if ( a < BigInteger.ZERO || b < BigInteger.ZERO)
            throw Exception("Only nature number supported.")

        if (a == BigInteger.ZERO || b == BigInteger.ZERO) return BigInteger.ZERO

        if (a == b) return a

        return if (a > b){
            val rem2 = a.rem(b)

             if (rem2 == BigInteger.ZERO)
                 b
            else
                 gcd(b, rem2)
        } else{
             gcd(b, a)
        }
    }

    fun gcd(a: Long, b: Long): Long {
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
        return lcm(a.toLong(), b.toLong())
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

    // a * x  +  b * y = gcd  solve x, y | integer
    fun gcdSolution(a: Long, b: Long, gcd: Long = gcd(a, b)): Pair<Long, Long>{
        if (a > b){
            if (gcd == 0L) return 0L to 0L

            if (b == 0L && a == 1L && gcd == 1L) return 1L to 0L

            if (a.rem(b) == gcd) return 1L to -1L * (a / b)

            val p = gcdSolution(b, a.rem(b), gcd)
            return p.second to (p.second * (a / b) * (-1L) + p.first)
        }

        return gcdSolution(b, a, gcd).let {
            it.second to it.first
        }
    }

    // invoke primeBefore to prepare the prime numbers first
    fun factorialPrime(n:Long): Map<Long, Int>{
        if (n <= 1)
            throw java.lang.Exception("ParameterException: n should be greater than 1")

        if(PRIMES.last() < n)
            primesBefore(n)

        if (PRIMES.binarySearch(n) >= 0){
            return mapOf(n to 1)
        }

        var tmp = n
        val res = mutableMapOf<Long, Int>()
        PRIMES.forEach {
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

    fun congruence(a: Long, c: Long, m: Long, start: Long = 0L, end : Long? = null): List<Long> {
        val g = gcd(a, m)
        if (c.rem(g) != 0L) return listOf()

        val u0 = gcdSolution(a, m, g).first
        val v = m / g

        var x0 = (u0 * (c/ g))

        while (x0 < 0){
            x0 += v
        }

        return ((start).until((end?:1) * g)).map { it * v + x0 }
    }

    // if multi > 2,  end = m * n * k
    fun multicongruence(b: Long, m: Long, c: Long, n:Long, end:Long? = null) : Set<Long> {
        return congruence(1, b, m, end = if (end == null) n else (end/m)).intersect(congruence(1, c, n, end = if (end == null) m else (end/n)))
    }

    fun euler_phi(n:Long, factorial: Map<Long, Int>? = null):Long {
        return (factorial?:factorialPrime(n)).keys.fold(n){acc, l -> acc / l * (l - 1L)  }
    }

    // the sum of all the divisors of n including 1 and n
    fun sigma(n:Long):Long {
        return factorialPrime(n).toList().fold(1L) {
            acc, pair ->
                acc * sumOfGeometricSequence(pair.second.toLong(), pair.first)
        }
    }

    fun sumOfGeometricSequence(pow: Long, increment: Long, start: Long = 1L):Long{
        if (increment == 1L)
            return pow * start

        return start * (increment.powOf(pow + 1) - 1L) / (increment - 1L)
    }

    @SuppressWarnings
    fun socialables(start: Long, order: Int): Set<Long>? {
        var s = start
        var flag = true
         (1..order).fold(mutableListOf<Long>(s)){
            acc, _ ->
                acc.apply {
                    if (flag){
                        if(s <= 1L){
                            flag = false
                        }
                        else{
                            s = sigma(s) - s
                            this.add(s)
                        }
                    }
                }
        }.let {
            if(flag && it.drop(1).indexOfFirst  { x -> x == start } >= 0)
                return it.toSet()

            return null
        }
    }

    fun modPow(a:Long, k:Long, m:Long): Long {
        var k0 = k
        var a0 = a
        var res = 1L

        while (k0 >= 1){
            val isOdd = (k0.rem(2) == 1L)
            if (isOdd)
                res = (res.times(a0)).rem(m)

            a0 = (a0.times(a0)).rem(m)
            k0 = if (isOdd) (k0 - 1) / 2 else (k0 / 2)
        }

        return res
    }

    // if false muss be composite
    fun checkLargePrime(n:Long, sampleBase: Iterable<Long>? = null):Boolean{
        return (sampleBase?:((1..10).map{ Random.nextLong(2L, n-1) })).all {
            modPow(it, n-1, n) == 1L
        }
    }

    fun solveRootsMod(k: Long, b: Long, m: Long, factorial: Map<Long, Int>? = null): Long {
        val phi = euler_phi(m, factorial)
        return modPow(b, gcdSolution(k, phi).first.let {
            var res = it
            while (res <= 0) res += phi
            res
        }, m)
    }

    fun pollars_rho(n:BigInteger, x: BigInteger = 2.toBigInteger(), y:BigInteger = 2.toBigInteger(), d: BigInteger = BigInteger.ONE, f: (BigInteger) -> BigInteger = {it.powOf(2) + BigInteger.ONE}): BigInteger {
        var x0 = x
        var y0 = y
        var d0 = d

        while (d0 == BigInteger.ONE){
            x0 = f(x0)
            y0 = f(f(y0))
            d0 = gcd(if((x0 - y0) > BigInteger.ZERO) x0 -y0 else y0 - x0 , n)
        }

        if (d0 == n)
            throw Exception("Error")

        return d0
    }

    fun pollars_rho(n:Long, x: Long = 2, y:Long = 2, d: Long = 1, f: (Long) -> Long = {it.powOf(2) + 1}): Long {
        var x0 = x
        var y0 = y
        var d0 = d

        while (d0 == 1L){
            x0 = f(x0)
            y0 = f(f(y0))
            d0 = gcd(if((x0 - y0) > 0) x0 -y0 else y0 - x0 , n)
        }

        if (d0 == n)
            throw Exception("Error")

        return d0
    }
}

fun Long.powOf(n:Long):Long {
    if (n == 1L)
        return this

    if (n.rem(2L) == 0L){
        val a = this.powOf(n.div(2L))
        return a.times(a)
    }else{
        val a = this.powOf((n-1).div(2L))
        return this.times(a).times(a)
    }
}

fun BigInteger.powOf(n:Long):BigInteger {
    if (n == 1L)
        return this

    if (n.rem(2L) == 0L){
        val a = this.powOf(n.div(2L))
        return a.times(a)
    }else{
        val a = this.powOf((n-1).div(2L))
        return this.times(a).times(a)
    }
}

fun Long.powOf(n:Int):Long{
    return this.powOf(n.toLong())
}