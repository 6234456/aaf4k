package eu.qiou.aaf4k.util.unit

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import java.util.*


abstract class ProtoUnit(open val scalar: UnitScalar = UnitScalar.UNIT, var locale: Locale = GlobalConfiguration.DEFAULT_LOCALE){
    abstract fun format(locale: Locale = this.locale): (Number) -> String

    open fun convertTo(unit: ProtoUnit): (Double) -> Double {
        return { n -> n * (scalar.scalar / unit.scalar.scalar) }
    }
}