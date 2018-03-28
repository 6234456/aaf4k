package eu.qiou.aaf4j.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.ArrayList

/**
 * class time span
 * @author   Qiou Yang
 * @since    1.0.0
 * @version  1.0.0
 */
class TimeSpan {
    var start: LocalDate? = null
    var end: LocalDate? = null

    private constructor() {}

    constructor(start: LocalDate, end: LocalDate) {
        this.start = start
        this.end = end

        if (start.compareTo(end) > 0) {
            println("Error: Period end before period start")
        }
    }

    fun drillDown(amount: Long, unit: ChronoUnit): ArrayList<TimeSpan> {
        val res = ArrayList<TimeSpan>()

        var start = this.start
        var tmp: LocalDate

        while (start!!.compareTo(this.end!!) <= 0) {
            tmp = start.plus(amount, unit)
            res.add(TimeSpan(start, tmp.minus(1, ChronoUnit.DAYS)))
            start = tmp
        }
        return res
    }

    override fun toString(): String {
        return "[" + this.start + ", " + this.end + "]"
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
