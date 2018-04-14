package eu.qiou.aaf4k.util.time

import java.time.LocalDate

data class TimeParameters(val timeSpan: TimeSpan?=null, val timePoint: LocalDate?=null) {
    constructor(timeSpan: TimeSpan):this(timeSpan, null)

    constructor(timePoint: LocalDate):this(null, timePoint)

    constructor(year: Int):this(TimeSpan.forYear(year), null)

    constructor(year: Int, month: Int):this(TimeSpan.forMonth(year, month), null)

    constructor(year: Int, month: Int, dayOfMonth:Int):this(null, LocalDate.of(year, month, dayOfMonth))


    val timeAttribute: TimeAttribute = when{
        timeSpan    != null && timePoint    == null     -> TimeAttribute.TIME_SPAN
        timePoint   != null && timeSpan     == null     -> TimeAttribute.TIME_POINT
        timePoint   == null && timeSpan     == null     -> TimeAttribute.CONSTANT
        else -> throw Exception("Specification Error: One and only one of the attribute timeSpan/timePoint should be specified!")
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