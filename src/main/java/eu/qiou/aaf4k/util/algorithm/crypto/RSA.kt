package eu.qiou.aaf4k.util.algorithm.crypto

import eu.qiou.aaf4k.util.algorithm.Algorithm
import eu.qiou.aaf4k.util.algorithm.powOf

class RSA(p: Long, q: Long, val k: Long) : Decipherable {
    private val m = p * q
    private val factorial = mapOf(p to 1, q to 1)
    private val phi = Algorithm.euler_phi(m, factorial)
    private val numberOfDigits = Algorithm.totalDigit(m) - 1

    init {
        assert( Algorithm.gcd(phi, k) == 1L)
        assert(numberOfDigits >= 2)
    }

    override fun encode(msg: String): List<Long> {
        // last element store the prefixing 0 of the second last element
        // 1_[00 121 11]
        val res = mutableListOf<Long>()

        var prefixingZero = 0

        // terminate clause can be executed only once
        var stop0 = true

        // group the digit to large long
        // the first element can not be mapped to 0
        fun process(acc: Long, l: Long, stop :Boolean): Long{
            val len = Algorithm.totalDigit(acc)
            val lenCurrent = Algorithm.totalDigit(l)
            val numberOfDigits1 = numberOfDigits - prefixingZero

            return if (len < numberOfDigits1){

                val toTake = numberOfDigits1 - len
                val operateLen = Math.min(toTake, lenCurrent)
                val sp = Algorithm.splitNumberAt(l, operateLen, true)

                if (toTake >= lenCurrent){
                    acc * (10L.powOf(operateLen.toLong())) + sp.first
                }else{
                    res.add(acc * (10L.powOf(operateLen.toLong())) + sp.first)
                    prefixingZero = if (sp.second == 0L) lenCurrent - toTake else 0
                    process(0L, sp.second, stop)
                }
            }else{
                res.add(acc)
                prefixingZero = 0
                l
            }.apply {
                if (stop && stop0){
                    res.add(this)
                    stop0 = false
                }
            }
        }
        msg.toCharArray().map { it.toLong() }.let {
            it.reduceIndexed{
                index, acc, l ->
                   process(acc, l, index == it.lastIndex)
            }
        }

        return res.map { Algorithm.modPow(it, k, m) } + listOf(prefixingZero.toLong())
    }

    override fun decode(i: List<Long>): String {

        val prefixing = i.last()
        val res = StringBuilder()

        fun processLong(l:Long):Long {
            if (Algorithm.totalDigit(l) >= 2){
                Algorithm.splitNumberAt(l, 2, true).let {
                    x ->
                    x.first.let { it ->
                        if (it in 65..122 || it == 32L){
                            res.append(it.toChar())
                            return processLong(x.second)
                        }else {
                            Algorithm.splitNumberAt(l, 3, true).let {
                                x ->
                                x.first.let {
                                    return if (it in 65..122 || it == 32L) {
                                        res.append(it.toChar())
                                        processLong(x.second)
                                    }else {
                                        l
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                return l
            }
        }

        fun process(acc:Long, l: Long, stop: Boolean): Long {
            val s = acc * 10L.powOf(
                    Algorithm.totalDigit(l) +
                    if (stop) prefixing else (numberOfDigits - Algorithm.totalDigit(l)).toLong()
            ) + l

            return processLong(s)
        }

        i.dropLast(1)
            .map { b -> Algorithm.solveRootsMod(k, b, m, factorial)}
            .reduceIndexed { index, acc, l ->
                process(acc, l, index == i.lastIndex - 1)
            }

        return res.toString()
    }

}