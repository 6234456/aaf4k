package eu.qiou.aaf4k.util.time

enum class TimeAttribute(val idx:Int) {
    TIME_SPAN(2),
    TIME_POINT(1),
    CONSTANT(0),
    MIXED(-1)
}