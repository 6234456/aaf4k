package eu.qiou.aaf4k.util.unit

import java.util.*


class EnumerationUnit(val unitSingular:String, var unitPlural:String = unitSingular, var unitNull:String = unitSingular):ProtoUnit() {
    override fun format(locale: Locale): (Number) -> String {
        return { a ->
            val b = a.toInt()

            when{
                b == 0 -> unitNull
                b == 1 -> unitSingular
                b > 1  -> String.format(unitPlural, b)
                else -> throw Exception("Illegal Parameter: $a less than zero")
            }
        }
    }
}