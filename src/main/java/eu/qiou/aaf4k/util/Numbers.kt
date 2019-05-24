package eu.qiou.aaf4k.util

import kotlin.math.abs
import kotlin.math.sign

fun Number.roundUpTo(place: Int): Double {
    return Math.round(this.toDouble() * Math.pow(10.0, place * 1.0)) / Math.pow(10.0, place * 1.0)
}

fun Iterable<Number>.irr(): Double {
    var guess = 0.0
    var lastGuess = 0.252

    var res = this.npv(guess)
    var lastRes = this.npv(lastGuess)

    var cnt = 1

    val precision = Math.pow(10.0, -6.0)
    val maxSteps = 1000
    val delta = 0.5

    val betterOff = { n1: Double, n2: Double ->
        Math.abs(n1) < Math.abs(n2)
    }

    var tmp: Double

    while (true) {
        if (cnt == maxSteps)
            throw Error("maximal loop reached!")

        if (Math.abs(res) < precision)
            break

        if (res.sign * lastRes.sign < 0) {
            tmp = (guess + lastGuess) / 2

            // different signs also keep the betterv value
            if (betterOff(res, lastRes)) {
                lastGuess = guess
                lastRes = res
            }

            guess = tmp
        } else {
            if (betterOff(res, lastRes)) {
                tmp = guess
                guess += delta * (guess - lastGuess).sign
                lastGuess = tmp
                lastRes = res
            } else {
                guess = lastGuess - delta * (guess - lastGuess).sign
            }
        }


        res = this.npv(guess)
        cnt++
    }

    return guess
}

fun Iterable<Number>.npv(r: Double, startFromP0: Boolean = true): Double {
    return this.mapIndexed { index, number -> number.toDouble() / Math.pow(1.0 + r, index.toDouble()) }
            .reduce { acc, e ->
                acc + e
            } / if (startFromP0) 1.0 else (1.0 + r)
}

fun Number.withinTolerance(tolerance: Double, other: Number): Boolean = abs((this.toDouble() - other.toDouble()) / this.toDouble()) <= abs(tolerance)