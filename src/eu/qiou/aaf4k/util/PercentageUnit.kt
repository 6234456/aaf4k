package eu.qiou.aaf4k.util

import java.util.*

class PercentageUnit : ProtoUnit {
    private constructor()

    companion object {
        private val percentageUnit = PercentageUnit()

        fun getInstance():PercentageUnit {
            return percentageUnit
        }
    }

    override fun format(locale: Locale): (Double) -> String {
        val f: (Double) -> String = { n -> String.format(locale, "%.2f%%", n)}
        return f
    }
}