package eu.qiou.aaf4k.util

import java.util.*

abstract class ProtoUnit(val scale: UnitScale = UnitScale.UNIT, var locale: Locale = Locale.getDefault()){
    abstract fun format(locale: Locale = this.locale): (Double) -> String

    fun convertTo(unit: ProtoUnit): (Double) -> Double {
        val f: (Double) -> Double  = { n -> n * (this.scale.scale  / unit.scale.scale) }
        return f
    }
}