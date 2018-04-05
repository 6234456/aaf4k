package eu.qiou.aaf4k.util.time

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.ArrayList

/**
 * class time span
 * @author   Qiou Yang
 * @since    1.0.0
 * @version  1.0.0
 */
data class TimeSpan(var start: LocalDate, var end: LocalDate) {

    private constructor():this(LocalDate.now(), LocalDate.now()){

    }

    init {
        assert(start.compareTo(end) <= 0)
    }

    operator fun contains(date: LocalDate):Boolean{
        return date.compareTo(start) >= 0 && date.compareTo(end) <= 0
    }

    fun drillDown(amount: Long, unit: ChronoUnit): ArrayList<TimeSpan> {
        val res = ArrayList<TimeSpan>()

        var tmp: LocalDate

        while (start.compareTo(end) <= 0) {
            tmp = start.plus(amount, unit)
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
