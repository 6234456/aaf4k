package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.time.TimeSpan
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class TimeSpanTest {
    val timeSpan = TimeSpan(LocalDate.of(2018,4,16), LocalDate.of(2018,4,23))

    @Test
    fun getChildren() {
        timeSpan.drillDownTo = Pair(1, ChronoUnit.MONTHS)
        println(timeSpan.getChildren())
    }

    @Test
    fun getParent() {
        println(timeSpan.getParent())
    }

    @Test
    fun getContainingHalfYear() {
        println(timeSpan.getContainingHalfYear())
    }

    @Test
    fun getContainingWeek() {
        println(TimeSpan(LocalDate.of(2018,4,16), LocalDate.of(2018,4,20)).getContainingWeek())
    }

    @Test
    fun getContainingQuarter() {
        println(timeSpan.getContainingQuarter())
    }
}