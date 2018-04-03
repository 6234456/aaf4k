package eu.qiou.aaf4k.util

import java.util.*

abstract class ProtoUnit(open val scale: UnitScale = UnitScale.UNIT, var locale: Locale = Locale.getDefault()){
    abstract fun format(locale: Locale = this.locale): (Double) -> String

    open fun convertTo(unit: ProtoUnit): (Double) -> Double {
        val f: (Double) -> Double  = { n -> n * (this.scale.scale  / unit.scale.scale) }
        return f
    }
}