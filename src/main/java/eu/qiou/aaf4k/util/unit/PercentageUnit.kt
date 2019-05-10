package eu.qiou.aaf4k.util.unit

import java.util.*


class PercentageUnit private constructor() : ProtoUnit() {

    companion object {
        private val percentageUnit = PercentageUnit()

        fun getInstance(): PercentageUnit {
            return percentageUnit
        }
    }

    override fun format(locale: Locale): (Number) -> String {
        return { n -> String.format(locale, "%.2f%%", n) }
    }
}