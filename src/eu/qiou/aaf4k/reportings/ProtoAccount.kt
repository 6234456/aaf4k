package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.reportings.AggregateAccount
import eu.qiou.aaf4k.reportings.ProtoReportingInfo
import eu.qiou.aaf4k.util.CurrencyUnit
import eu.qiou.aaf4k.util.ProtoUnit


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
                         var localAccountID: String = id.toString()){

    var displayUnit: ProtoUnit = reportingInfo.displayUnit
    var superAccount: AggregateAccount? = null

    override fun equals(other: Any?): Boolean {
        if( other is ProtoAccount){
             return other.id == this.id
        }
         return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "[" + localAccountID + " " + name + "] : " + displayUnit.format()(unit.convertTo(displayUnit)(this.value * unit.scale.scale / Math.pow(10.0, decimalPrecision * 1.0))) + displayUnit.scale.token
    }
}