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

    data class ChronoSpan(val amount:Long = 1, val unit: ChronoUnit = ChronoUnit.MONTHS)

    var drillDownTo = ChronoSpan(1, ChronoUnit.MONTHS)
    var rollUpTo = setOf<ChronoSpan>(ChronoSpan(1, ChronoUnit.YEARS))

    private constructor():this(LocalDate.now(), LocalDate.now())


    init {
        assert(start <= end)
    }

    fun getChildren(): List<TimeSpan> {
        return this.drillDown()
    }

    fun getParents(): Collection<TimeSpan>? {
        return rollUp()
    }


    operator fun contains(date: LocalDate):Boolean{
        return date in start..end
    }

    operator fun contains(span: TimeSpan): Boolean {
        return span.start >= start && span.end <= end
    }

    operator fun plus(period: Period):TimeSpan {
        return TimeSpan(start.plus(period), end.plus(period))
    }

    fun plus(amount: Long, unit: ChronoUnit): TimeSpan {
        var s = start.plus(amount, unit)
        var e = end.plus(amount, unit)

        if (unit == ChronoUnit.MONTHS) {
            if (start.isEndOfMonth()) s = s.toEndOfMonth()
            if (end.isEndOfMonth()) e = e.toEndOfMonth()
        }

        return TimeSpan(s, e)
    }

    operator fun minus(period: Period):TimeSpan {
        return TimeSpan(start.minus(period), end.minus(period))
    }

    fun rollForward(unit: ChronoUnit = drillDownTo.unit): TimeSpan {
        return this.plus(drillDown(1, unit).size.toLong(), unit)
    }

    fun drillDown(interval: Long = drillDownTo.amount, unit: ChronoUnit = drillDownTo.unit): ArrayList<TimeSpan> {
        val res = ArrayList<TimeSpan>()
        var start = start

        var tmp: LocalDate

        while (start <= end) {
            tmp = start.plus(interval, unit)
            res.add(TimeSpan(start, tmp.minus(1, ChronoUnit.DAYS)))
            start = tmp
        }
        return res
    }

    fun rollUp(): List<TimeSpan> {
        return rollUpTo.map{
            when(it) {
                ChronoSpan(1, ChronoUnit.YEARS) -> getContainingYear()
                ChronoSpan(6, ChronoUnit.MONTHS) ->getContainingHalfYear()
                ChronoSpan(4, ChronoUnit.MONTHS) ->getContainingQuarter()
                ChronoSpan(1, ChronoUnit.MONTHS) ->getContainingMonth()
                ChronoSpan(1, ChronoUnit.WEEKS) ->getContainingWeek()
                else -> throw Exception("Unknown Unit: $it.")
            }
        }
    }

    fun isInOneDay():Boolean {
        return this.start == this.end
    }

    fun isInOneYear():Boolean {
        return this.start.year == this.end.year
    }

    fun isInOneWeek():Boolean {
        return drillDown(1, ChronoUnit.DAYS).count() <= 7 && start.dayOfWeek <= end.dayOfWeek
    }

    fun isInOneHalfYear():Boolean {
        return isInOneYear() && (this.start.month.value * 1.0 - 6.5) * (this.end.month.value *1.0 - 6.5) > 0
    }

    fun isInOneMonth():Boolean {
        return isInOneYear() && this.start.month == this.end.month
    }

    fun isInOneQuarter():Boolean {
        return isInOneYear() &&
                this.end.month.value - this.start.month.value <= 2 &&
                (this.end.monthValue.rem(3) == 0 || this.end.monthValue.rem(3) >= this.start.monthValue.rem(3))
    }

    fun getContainingYear():TimeSpan {
        if(! isInOneYear()) throw Exception("${this} expands across multiple years")
        return forYear(this.start.year)
    }

    fun getContainingHalfYear():TimeSpan {
        if(! isInOneHalfYear()) throw Exception("${this} expands across multiple half-years")
        return forHalfYear(this.start.year, this.start.month.value <= 6)
    }

    fun getContainingMonth():TimeSpan {
        if(! isInOneMonth()) throw Exception("${this} expands across multiple months")
        return forMonth(this.start.year, this.start.monthValue)
    }

    fun getContainingWeek():TimeSpan {
        if(! isInOneWeek()) throw Exception("${this} expands across multiple weeks")
        val mondayOftheWeek = this.start + ChronoUnit.DAYS * (this.start.dayOfWeek.value - 1 )
        return TimeSpan(mondayOftheWeek, mondayOftheWeek + ChronoUnit.DAYS * 6)
    }

    fun getContainingQuarter():TimeSpan {
        if(! isInOneQuarter()) throw Exception("${this} expands across multiple quarters")
        return forQuarter(this.start.year, Math.ceil(this.start.monthValue * 1.0 / 3.0).toInt())
    }

    override fun toString(): String {
        return "[$start, $end]"
    }

    companion object {

        fun forYear(year: Int): TimeSpan {
            return TimeSpan(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))
        }

        fun forHalfYear(year: Int, firstHalf: Boolean = true): TimeSpan {
            return if(firstHalf) TimeSpan(LocalDate.of(year, 1, 1), LocalDate.of(year, 6, 30))
                        else TimeSpan(LocalDate.of(year, 7, 1), LocalDate.of(year, 12, 31))
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


fun LocalDate.isEndOfMonth() = this.plusDays(1).month != this.month
fun LocalDate.toEndOfMonth() = this.plusMonths(1).withDayOfMonth(1).minusDays(1)!!

fun LocalDate.startOfNextMonth() = this.withDayOfMonth(1).plusMonths(1)!!
fun LocalDate.endOfNextMonth() = this.startOfNextMonth().minusDays(1)

fun LocalDate.to(ends: LocalDate, withIntervalUnit: ChronoUnit = ChronoUnit.YEARS, withIntervalAmount: Int = 1): List<LocalDate> {
    val res = mutableListOf<LocalDate>()
    var tmp = this
    var cnt = 1

    while (true) {
        if (tmp > ends) {
            break
        } else {
            res.add(tmp)
            tmp = this + (withIntervalUnit * (withIntervalAmount * cnt))
            cnt++
        }
    }

    return res
}

fun LocalDate.ofNext(terms: Int, withIntervalUnit: ChronoUnit = ChronoUnit.YEARS, withIntervalAmount: Int = 1): List<LocalDate> {
    val res = mutableListOf<LocalDate>()
    var tmp = this

    for (cnt in 1..terms) {
        res.add(tmp)
        tmp = this + (withIntervalUnit * (withIntervalAmount * cnt))

        if (this.isEndOfMonth() && withIntervalUnit == ChronoUnit.MONTHS)
            tmp = tmp.toEndOfMonth()
    }

    return res
}

fun ChronoUnit.toPercentageOfYear(): Double = when (this) {
    ChronoUnit.YEARS -> 1.0
    ChronoUnit.MONTHS -> 1.0 / 12
    ChronoUnit.DAYS -> 1.0 / 360
    ChronoUnit.WEEKS -> 1.0 / 52
    ChronoUnit.DECADES -> 10.0
    else -> throw Exception("unimplemented method")
}
