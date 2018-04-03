package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.util.CurrencyUnit
import eu.qiou.aaf4k.util.PercentageUnit
import eu.qiou.aaf4k.util.ProtoUnit
import eu.qiou.aaf4k.util.io.JSONable
import java.util.*


/**
 * @author Qiou Yang
 * @since 1.0.0
 * @param id the internal id of the account
 * @param name the internal name of the account
 * @param unit specify the unit stored in the value
 */

open class ProtoAccount( var id: Int , var name: String , open var value:Long = 0, var unit: ProtoUnit = CurrencyUnit(),
                         var decimalPrecision: Int = 2, var desc: String="",
                         var reportingInfo: ProtoReportingInfo = ProtoReportingInfo(), var hasSubAccounts: Boolean = false, var hasSuperAccounts: Boolean = false,
                         var localAccountID: String = id.toString()): Comparable<ProtoAccount>, JSONable{
    var displayUnit: ProtoUnit = reportingInfo.displayUnit
    var superAccount: AggregateAccount? = null

    var displayValue: Double
    get() = value / Math.pow(10.0, decimalPrecision.toDouble())
    set(v) {setDisplayValue(v)}

    private fun setDisplayValue(v : Double) : Double{
        if(displayUnit.scale.equals(this.unit.scale))
            this.value = Math.round(v * Math.pow(10.0, decimalPrecision.toDouble()))

        return v
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

    override fun compareTo(other: ProtoAccount): Int {
        return id.compareTo(other.id)
    }

    override fun toString(): String {
        return "[$localAccountID $name] : " +  "${if(displayUnit is CurrencyUnit) (displayUnit as CurrencyUnit).getSymbol() else ""} "  + displayUnit.format()(unit.convertTo(displayUnit)(this.displayValue * unit.scale.scale ))
    }

    override fun toJSON():String {
        return "{id: $id, name: '$name', value: $value, displayValue: $displayValue, decimalPrecision: $decimalPrecision, desc: '$desc', hasSubAccounts: $hasSubAccounts, hasSuperAccounts: $hasSuperAccounts, localAccountID:$localAccountID, scale: ${unit.scale}, isCurrency: ${unit is CurrencyUnit}, isPercentage: ${unit is PercentageUnit}}"
    }

}