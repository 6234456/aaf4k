package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.time.times
import org.junit.Test

import org.junit.Assert.*
import java.time.temporal.ChronoUnit

class TimeSpanTest {

    @Test
    fun plus() {
        val span1 = TimeSpan.forYear(2017)
        println(span1 + (ChronoUnit.YEARS * 1))
        println(span1 - (ChronoUnit.YEARS * 1))
        println(span1 + (ChronoUnit.MONTHS * 6))
    }
}