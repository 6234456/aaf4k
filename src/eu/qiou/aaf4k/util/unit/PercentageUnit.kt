package eu.qiou.aaf4k.util.unit

import java.util.*

class PercentageUnit : ProtoUnit {
    private constructor()

    companion object {
        private val percentageUnit = PercentageUnit()

        fun getInstance(): PercentageUnit {
            return percentageUnit
        }
    }

    override fun format(locale: Locale): (Number) -> String {
        val f: (Number) -> String = { n -> String.format(locale, "%.2f%%", n)}
        return f
    }
}