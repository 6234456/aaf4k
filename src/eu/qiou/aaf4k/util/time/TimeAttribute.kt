package eu.qiou.aaf4k.util.time


enum class TimeAttribute(val index:Int) {
    TIME_SPAN(2),
    TIME_POINT(1),
    CONSTANT(0),
}

fun parseTimeAttribute(n: Number): TimeAttribute {
    return when (n.toInt()) {
        TimeAttribute.CONSTANT.index -> TimeAttribute.CONSTANT
        TimeAttribute.TIME_POINT.index -> TimeAttribute.TIME_POINT
        TimeAttribute.TIME_SPAN.index -> TimeAttribute.TIME_SPAN
        else -> throw Exception("IllegalParameter: $n can not be parsed to TimeAttribute")
    }
}