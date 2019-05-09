package eu.qiou.aaf4k.util.time

import eu.qiou.aaf4k.util.io.JSONable
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class TimeParameters(val timeSpan: TimeSpan? = null, val timePoint: LocalDate? = null) : JSONable {

    constructor(timeSpan: TimeSpan):this(timeSpan, null)

    constructor(timePoint: LocalDate):this(null, timePoint)

    constructor(year: Int):this(TimeSpan.forYear(year), null)

    constructor(year: Int, month: Int):this(TimeSpan.forMonth(year, month), null)

    constructor(year: Int, month: Int, dayOfMonth:Int):this(null, LocalDate.of(year, month, dayOfMonth))

    val end: LocalDate
    get() {
        return when(this.timeAttribute){
            TimeAttribute.TIME_POINT -> this.timePoint!!
            TimeAttribute.TIME_SPAN  -> this.timeSpan!!.end
            else -> throw Exception("Specification Error: One and only one of the attribute timeSpan/timePoint should be specified!")
        }
    }

    val start: LocalDate
    get() {
        return when(this.timeAttribute){
            TimeAttribute.TIME_POINT -> this.timePoint!!
            TimeAttribute.TIME_SPAN  -> this.timeSpan!!.start
            else -> throw Exception("Specification Error: One and only one of the attribute timeSpan/timePoint should be specified!")
        }
    }

    val timeAttribute: TimeAttribute = when{
        timeSpan    != null && timePoint    == null     -> TimeAttribute.TIME_SPAN
        timePoint   != null && timeSpan     == null     -> TimeAttribute.TIME_POINT
        timePoint   == null && timeSpan     == null     -> TimeAttribute.CONSTANT
        else -> throw Exception("Specification Error: One and only one of the attribute timeSpan/timePoint should be specified!")
    }

    fun rollForward(unit: ChronoUnit = ChronoUnit.MONTHS): TimeParameters {
        return when (timeAttribute) {
            TimeAttribute.CONSTANT -> this
            TimeAttribute.TIME_SPAN -> TimeParameters(timeSpan!!.rollForward(unit))
            TimeAttribute.TIME_POINT -> TimeParameters(timePoint!!.plus(1L, unit))
        }
    }

    operator fun contains(timeParameters: TimeParameters):Boolean{
        return this.start <= timeParameters.start && this.end >= timeParameters.end
    }

    operator fun contains(date: LocalDate):Boolean{
        return this.start <= date && this.end >= date
    }

    operator fun contains(timeSpan: TimeSpan):Boolean{
        return this.start <= timeSpan.start && this.end >= timeSpan.end
    }

    fun containingYear(): TimeParameters {

        if (this.start.year != this.end.year)
            throw Exception("The time span over 1 year!")

        return forYear(this.start.year)
    }

    fun containingMonth(): TimeParameters {

        if ((this.start.year != this.end.year) or (this.start.monthValue != this.end.monthValue))
            throw Exception("The time span over 1 month!")

        return forMonth(this.start.year, this.start.monthValue)
    }

    override fun toJSON(): String {
        return """{"type":${timeAttribute.index}""" +
                when (timeAttribute) {
                    TimeAttribute.TIME_SPAN, TimeAttribute.TIME_POINT -> """, "start":"${this.start}", "end":"${this.end}" }"""
                    TimeAttribute.CONSTANT -> """}"""
                }

    }

    companion object {
        fun realTime():TimeParameters {
            return TimeParameters(timePoint = LocalDate.now())
        }

        fun forYear(year:Int):TimeParameters {
            return TimeParameters(timeSpan = TimeSpan.forYear(year))
        }
        fun forMonth(year:Int, month:Int):TimeParameters {
            return TimeParameters(timeSpan = TimeSpan.forMonth(year, month))
        }
        fun forQuarter(year:Int, quarter: Int):TimeParameters {
            return TimeParameters(timeSpan = TimeSpan.forQuarter(year, quarter))
        }
    }
}