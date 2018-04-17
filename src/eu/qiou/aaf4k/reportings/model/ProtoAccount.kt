package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.strings.CollectionToString
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

open class ProtoAccount(val id: Int, val name: String,
                        val subAccounts: MutableSet<ProtoAccount>? = null,
                        var decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION,
                        var value:Long = ((if(subAccounts == null) 0.0 else subAccounts.fold(0.0){ acc, e -> acc + e.displayValue}) * Math.pow(10.0, decimalPrecision.toDouble())).toLong(),
                        val unit: ProtoUnit = CurrencyUnit(),
                        val desc: String="",
                        val timeParameters: TimeParameters? = null,
                        val entity: ProtoEntity? = null
                        ): Comparable<ProtoAccount>, JSONable, Drilldownable{

    constructor(id: Int, name: String, decimalPrecision: Int, value: Long, unit: ProtoUnit, desc: String, timeParameters: TimeParameters?, entity: ProtoEntity?):
            this(id, name, null, value= value, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity)


    override fun add(child: Drilldownable): Drilldownable {
        if (this.hasChildren()){
            if(child is ProtoAccount){
                child.register(this)
                subAccounts!!.add(child)
            }
        }
        return this
    }

    override fun remove(child: Drilldownable): Drilldownable {
        if (this.hasChildren()){
            if(child is ProtoAccount){
                child.unregister(this)
                subAccounts!!.remove(child)
            }
        }
        return this
    }


    override fun getChildren(): Collection<ProtoAccount>? {
        return subAccounts
    }

    override fun getParent(): Collection<ProtoAccount>? {
        return superAccounts
    }


    var displayUnit:ProtoUnit = CurrencyUnit()

    var hasSubAccounts: Boolean = false
        get() {
            if(subAccounts == null)
                return false

            return subAccounts.count() > 0
        }
        private set


    var superAccounts: MutableSet<ProtoAccount>? = null

    var hasSuperAccounts: Boolean = false
    get() {
        if(superAccounts == null)
           return false

        return superAccounts!!.count() > 0
    }
    private set

    var localAccountID: String = id.toString()

    var displayValue: Double = 0.0
    get() = roundUpTo(when{
            unit is CurrencyUnit    -> unit.convertFxTo(displayUnit,timeParameters)(storeToDisplay())
            else                    -> unit.convertTo(displayUnit)(storeToDisplay())
        })
    set(v) {
        // display value of the drilldownables are not settable
        if(subAccounts == null){
            if(displayUnit.scalar.equals(this.unit.scalar))
                this.value = Math.round(v * Math.pow(10.0, decimalPrecision.toDouble()))
            field = v
        }
    }

    fun register(superAccount: ProtoAccount){
        if(superAccounts == null){
            superAccounts = mutableSetOf(superAccount)
        }else{
            superAccounts!!.add(superAccount)
        }
    }

    fun unregister(superAccount: ProtoAccount){
        if(superAccounts != null){
            superAccounts!!.remove(superAccount)
        }
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

    private fun titel():String{
        return "[$id $name]"
    }

    override fun toString(): String {
        if (this.hasChildren()){
            return CollectionToString.structuredToStr(this, 0, ProtoAccount::toString as Drilldownable.() -> String, ProtoAccount::titel as Drilldownable.() -> String)
        }

        return "($localAccountID $name) : ${displayUnit.format()(displayValue)}"
    }

    override fun toJSON():String {
        if(this.hasChildren()){
            return "{id: $id, name: '$name', value: $value, displayValue: $displayValue, decimalPrecision: $decimalPrecision, desc: '$desc', hasSubAccounts: $hasSubAccounts, hasSuperAccounts: $hasSuperAccounts, localAccountID: $localAccountID, subAccounts: " +
                    CollectionToString.mkJSON(subAccounts as Iterable<JSONable>, ",\n")  + "}"
        }

        return "{id: $id, name: '$name', value: $value, displayValue: $displayValue, decimalPrecision: $decimalPrecision, desc: '$desc', hasSubAccounts: $hasSubAccounts, hasSuperAccounts: $hasSuperAccounts, localAccountID:$localAccountID, scalar: ${unit.scalar}, isCurrency: ${unit is CurrencyUnit}, isPercentage: ${unit is PercentageUnit}}"
    }

}