package eu.qiou.aaf4k.util.time

import java.time.LocalDate

data class TimeParameters(val timeSpan: TimeSpan?=null, val timePoint: LocalDate?=null) {
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
    }
}