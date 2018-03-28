package eu.qiou.aaf4k.util

import java.util.*

class PercentageUnit() : ProtoUnit() {
    override fun format(locale: Locale): (Double) -> String {
        val f: (Double) -> String = { n -> String.format(locale, "%.2f%%", n)}
        return f
    }
}