package eu.qiou.aaf4k.util.unit

import eu.qiou.aaf4k.reportings.accounting.Accounting
import java.util.*


abstract class ProtoUnit(open val scalar: UnitScalar = UnitScalar.UNIT, var locale: Locale = Accounting.DEFAULT_LOCALE){
    abstract fun format(locale: Locale = this.locale): (Number) -> String


    open fun convertTo(unit: ProtoUnit): (Double) -> Double {
        val f: (Double) -> Double  = { n -> n * (this.scalar.scalar  / unit.scalar.scalar) }
        return f
    }
}