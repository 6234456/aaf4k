package eu.qiou.aaf4k.util.time

import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.util.*


/**
 * class time span
 * @author   Qiou Yang
 * @since    1.0.0
 * @version  1.0.0
 */
data class TimeSpan(val start: LocalDate, val end: LocalDate) {

    private constructor():this(LocalDate.now(), LocalDate.now())

    init {
        assert(start.compareTo(end) <= 0)
    }

    operator fun contains(date: LocalDate):Boolean{
        return date.compareTo(start) >= 0 && date.compareTo(end) <= 0
    }

    operator fun contains(span: TimeSpan):Boolean{
        return span.start.compareTo(start) >= 0 && span.end.compareTo(end) <= 0
    }

    operator fun plus(period: Period):TimeSpan {
        return TimeSpan(start.plus(period), end.plus(period))
    }

    operator fun minus(period: Period):TimeSpan {
        return TimeSpan(start.minus(period), end.minus(period))
    }

    fun drillDown(interval: Long, unit: ChronoUnit): ArrayList<TimeSpan> {
        val res = ArrayList<TimeSpan>()
        var start = start

        var tmp: LocalDate

        while (start.compareTo(end) <= 0) {
            tmp = start.plus(interval, unit)
            res.add(TimeSpan(start, tmp.minus(1, ChronoUnit.DAYS)))
            start = tmp
        }
        return res
    }

    override fun toString(): String {
        return "[$start, $end]"
    }

    companion object {

        fun forYear(year: Int): TimeSpan {
            return TimeSpan(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))
        }

        fun forMonth(year: Int, month: Int): TimeSpan {
            val start = LocalDate.of(year, month, 1)
            return TimeSpan(start, LocalDate.of(year, month, start.lengthOfMonth()))
        }

        fun forQuarter(year: Int, quarter: Int): TimeSpan {
            val start = LocalDate.of(year, quarter * 3 - 2, 1)
            val end = LocalDate.of(year, quarter * 3, 1)

            return TimeSpan(start, LocalDate.of(year, quarter * 3, end.lengthOfMonth()))
        }
    }
}

operator fun ChronoUnit.times(n: Int):Period = when{
    this == ChronoUnit.DAYS -> Period.ofDays(n)
    this == ChronoUnit.WEEKS -> Period.ofWeeks(n)
    this == ChronoUnit.MONTHS -> Period.ofMonths(n)
    this == ChronoUnit.YEARS -> Period.ofYears(n)
    this == ChronoUnit.DECADES -> Period.ofYears(n * 10)
    else -> throw Exception("unimplemented method")
}

operator fun LocalDate.plus(period: Period) = this.plus(period)
operator fun LocalDate.minus(period: Period) = this.minus(period)