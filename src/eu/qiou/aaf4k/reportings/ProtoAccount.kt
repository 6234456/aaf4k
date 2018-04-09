package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.PercentageUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit


/**
 * @author Qiou Yang
 * @since 1.0.0
 * @param id the internal id of the account
 * @param name the internal name of the account
 * @param unit specify the unit stored in the value
 */

open class ProtoAccount(val id: Int, val name: String, open var value:Long = 0, val unit: ProtoUnit = CurrencyUnit(),
                        var decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION, val desc: String="", var timeParameters: TimeParameters? = null,
                        var displayUnit:ProtoUnit = CurrencyUnit(), var hasSubAccounts: Boolean = false, var hasSuperAccounts: Boolean = false,
                        var localAccountID: String = id.toString()): Comparable<ProtoAccount>, JSONable{
    var superAccount: AggregateAccount? = null

    var displayValue: Double = 0.0
    get() = roundUpTo(when{
            unit is CurrencyUnit    -> unit.convertFxTo(displayUnit,timeParameters)(storeToDisplay())
            else                    -> unit.convertTo(displayUnit)(storeToDisplay())
        })

    set(v) {
        if(displayUnit.scalar.equals(this.unit.scalar))
            this.value = Math.round(v * Math.pow(10.0, decimalPrecision.toDouble()))
        field = v
    }

    private fun roundUpTo(v: Double, decimalPlace: Int = decimalPrecision):Double{
        return Math.round(v * Math.pow(10.0, decimalPlace.toDouble())) / Math.pow(10.0, decimalPlace.toDouble())
    }

    private fun storeToDisplay(v: Long = value, decimalPlace: Int = decimalPrecision): Double {
        return v.toDouble()/ Math.pow(10.0, decimalPlace.toDouble())
    }

    override fun equals(other: Any?): Boolean {
        if( other is ProtoAccount){
             return other.id == this.id
        }
         return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    // for sorting
    override operator fun compareTo(other: ProtoAccount): Int {
        return id.compareTo(other.id)
    }

    override fun toString(): String {
        return "[$localAccountID $name] : ${displayUnit.format()(displayValue)}"
    }

    override fun toJSON():String {
        return "{id: $id, name: '$name', value: $value, displayValue: $displayValue, decimalPrecision: $decimalPrecision, desc: '$desc', hasSubAccounts: $hasSubAccounts, hasSuperAccounts: $hasSuperAccounts, localAccountID:$localAccountID, scalar: ${unit.scalar}, isCurrency: ${unit is CurrencyUnit}, isPercentage: ${unit is PercentageUnit}}"
    }

}