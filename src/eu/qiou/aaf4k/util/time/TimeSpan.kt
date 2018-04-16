package eu.qiou.aaf4k.util.time

import eu.qiou.aaf4k.reportings.model.Drilldownable
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
data class TimeSpan(val start: LocalDate, val end: LocalDate):Drilldownable {

    var drillDownTo = Pair<Long, ChronoUnit>(1, ChronoUnit.MONTHS)
    var rollUpTo = setOf<Pair<Long, ChronoUnit>>(Pair(1, ChronoUnit.YEARS))

    private constructor():this(LocalDate.now(), LocalDate.now())

    override fun add(child: Drilldownable): Drilldownable {
        throw Exception("TimeSpan immutable!")
    }

    override fun remove(child: Drilldownable): Drilldownable {
        throw Exception("TimeSpan immutable!")
    }


    init {
        assert(start.compareTo(end) <= 0)
    }

    override fun getChildren(): Collection<TimeSpan>? {
        return this.drillDown(drillDownTo.first, drillDownTo.second)
    }

    override fun getParent(): Collection<TimeSpan>? {
        return rollUp()
    }


    operator fun contains(date: LocalDate):Boolean{
        return date.compareTo(start) >= 0 && date.compareTo(end) <= 0
    }

    override operator fun contains(span: Drilldownable):Boolean{
        if (span is TimeSpan)
            return span.start.compareTo(start) >= 0 && span.end.compareTo(end) <= 0

        return false
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

    fun rollUp(): List<TimeSpan> {
        return rollUpTo.map{
            when(it) {
                Pair<Long, ChronoUnit>(1, ChronoUnit.YEARS) -> getContainingYear()
                Pair<Long, ChronoUnit>(6, ChronoUnit.MONTHS) ->getContainingHalfYear()
                Pair<Long, ChronoUnit>(4, ChronoUnit.MONTHS) ->getContainingQuarter()
                Pair<Long, ChronoUnit>(1, ChronoUnit.MONTHS) ->getContainingMonth()
                Pair<Long, ChronoUnit>(1, ChronoUnit.WEEKS) ->getContainingWeek()
                else -> throw Exception("Unknow Unit: ${it}.")
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
        return TimeSpan.forYear(this.start.year)
    }

    fun getContainingHalfYear():TimeSpan {
        if(! isInOneHalfYear()) throw Exception("${this} expands across multiple half-years")
        return TimeSpan.forHalfYear(this.start.year, this.start.month.value <= 6)
    }

    fun getContainingMonth():TimeSpan {
        if(! isInOneMonth()) throw Exception("${this} expands across multiple months")
        return TimeSpan.forMonth(this.start.year, this.start.monthValue)
    }

    fun getContainingWeek():TimeSpan {
        if(! isInOneWeek()) throw Exception("${this} expands across multiple weeks")
        val mondayOftheWeek = this.start + ChronoUnit.DAYS * (this.start.dayOfWeek.value - 1 )
        return TimeSpan(mondayOftheWeek, mondayOftheWeek + ChronoUnit.DAYS * 6)
    }

    fun getContainingQuarter():TimeSpan {
        if(! isInOneQuarter()) throw Exception("${this} expands across multiple quarters")
        return TimeSpan.forQuarter(this.start.year, Math.ceil(this.start.monthValue * 1.0 / 3.0).toInt() )
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

operator fun LocalDate.plus(period: Period) = this.plus(period)
operator fun LocalDate.minus(period: Period) = this.minus(period)
