package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.reportings.model.ProtoAccount.Builder.Companion.VALUE_SETTER_AGGREGATE
import eu.qiou.aaf4k.reportings.model.ProtoAccount.Builder.Companion.VALUE_SETTER_BASIC
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
 *
 * @param isStatistical for aggregate account, will not be checked for duplicity
 */

open class ProtoAccount(val id: Int, open val name: String,
                        val subAccounts: MutableSet<out ProtoAccount>? = null,
                        val decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION,
                        val value: Long? = null,
                        val unit: ProtoUnit = CurrencyUnit(),
                        val desc: String = "",
                        val timeParameters: TimeParameters? = null,
                        val entity: ProtoEntity? = null,
                        val isStatistical: Boolean = false
                        ): Comparable<ProtoAccount>, JSONable, Drilldownable{

    /**
     *  for the atomic account, specify the value
     */
    constructor(id: Int, name: String, value: Long, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION, unit: ProtoUnit = CurrencyUnit(), desc: String = "", timeParameters: TimeParameters? = null, entity: ProtoEntity? = null, isStatistical: Boolean = false) :
            this(id, name, null, decimalPrecision = decimalPrecision, value = value, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical)

    /**
     *  for the aggregate account, specify subAccounts
     *  if ignore the parameter subAccounts, empty set by default
     */
    constructor(id: Int, name: String, subAccounts: MutableSet<ProtoAccount>, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION, unit: ProtoUnit = CurrencyUnit(), desc: String = "", timeParameters: TimeParameters? = null, entity: ProtoEntity? = null, isStatistical: Boolean = false) :
            this(id, name, subAccounts = subAccounts, decimalPrecision = decimalPrecision, value = null, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical)

    constructor(id: Int, name: String, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION, unit: ProtoUnit = CurrencyUnit(), desc: String = "", timeParameters: TimeParameters? = null, entity: ProtoEntity? = null, isStatistical: Boolean = false) :
            this(id, name, subAccounts = mutableSetOf(), decimalPrecision = decimalPrecision, value = null, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical)

    init {
        if (value == null && subAccounts == null)
            throw Exception("either value or subAccounts should be specified.")

        if (value != null && subAccounts != null)
            throw Exception("Only one of the parameters, value or subAccounts, should be specified.")
    }

    override fun add(child: Drilldownable): Drilldownable {
        if (isAggregate) {
            if(child is ProtoAccount){
                child.register(this)
                (subAccounts!! as MutableSet<ProtoAccount>).add(child)
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

    override fun getParents(): Collection<ProtoAccount>? {
        return superAccounts
    }

    var hasSubAccounts: Boolean = false
        get() = hasChildren()
        private set

    var superAccounts: MutableSet<ProtoAccount>? = null
    var hasSuperAccounts: Boolean = false
    get() = hasParent()
    private set

    // can be re-implemented based on Locale
    var localAccountID: String = id.toString()
    var localAccountName: String = id.toString()

    val isAggregate:Boolean = subAccounts != null

    var decimalValue: Double = 0.0
        get() {
            return if (isAggregate)
                subAccounts!!.fold(0.0) { acc, e ->
                    acc + e.decimalValue
                }
            else
                value!!.toDouble() / Math.pow(10.0, decimalPrecision.toDouble())
        }
        private set

    var displayUnit: ProtoUnit = CurrencyUnit()
    var displayValue: Double = 0.0
        get() = roundUpTo(when {
            this.unit is CurrencyUnit -> unit.convertFxTo(displayUnit, timeParameters)(decimalValue)
            else -> unit.convertTo(displayUnit)(decimalValue)
        })
        private set


    var textValue: String? = displayUnit.format()(displayValue)

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

            if(superAccounts!!.count() == 0)
                superAccounts = null
        }
    }

    fun findChildByID(id: Int): ProtoAccount? {
        if (!hasChildren()) {
            if (this.id == id) {
                return this
            }
        } else {
            for (i in subAccounts!!) {
                i.findChildByID(id)
            }
        }

        return null
    }

    private fun roundUpTo(v: Double, decimalPlace: Int = decimalPrecision):Double{
        return Math.round(v * Math.pow(10.0, decimalPlace.toDouble())) / Math.pow(10.0, decimalPlace.toDouble())
    }

    fun toBuilder(): Builder {
        return builder(this)
    }


    // the general method to transform a ProtoAccount
    fun deepCopy(callbackAtomicAccount: (ProtoAccount) -> ProtoAccount): ProtoAccount {
        if (this.isAggregate) {
            return this.toBuilder().setType(VALUE_SETTER_AGGREGATE).setValue(
                    this.subAccounts!!.map { it.deepCopy(callbackAtomicAccount) }.toMutableSet()
            ).build()
        } else {
            return callbackAtomicAccount(this)
        }
    }

    fun deepCopy(data: Map<Int, Double>, updateMethod: (Double, Double) -> Double = { valueNew, valueOld -> valueNew }): ProtoAccount {
        val callback: (ProtoAccount) -> ProtoAccount = {
            it.update(data, updateMethod)
        }
        return deepCopy(callback)
    }

    fun deepCopy(entry: ProtoEntry, updateMethod: (Double, Double) -> Double = { valueNew, valueOld -> valueNew }): ProtoAccount {
        return deepCopy(entry.toDataMap(), updateMethod)
    }

    fun deepCopy(category: ProtoCategory, updateMethod: (Double, Double) -> Double = { valueNew, valueOld -> valueNew }): ProtoAccount {
        return deepCopy(category.toDataMap(), updateMethod)
    }

    fun update(map: Map<Int, Double>, callback: (Double, Double) -> Double = { valueNew, valueOld -> valueNew }): ProtoAccount {
        if (map.containsKey(id))
            return toBuilder().setValue(callback(map.getValue(id), this.decimalValue)).build()
        else
            return shallowCopy()
    }

    fun shallowCopy(): ProtoAccount {
        return toBuilder().build()
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

    @Suppress("UNCHECKED_CAST")
    override fun toString(): String {
        if (this.hasChildren()){
            return CollectionToString.structuredToStr(this, 0, ProtoAccount::toString as Drilldownable.() -> String, ProtoAccount::titel as Drilldownable.() -> String)
        }

        return "($localAccountID $name) : $textValue"
    }

    override fun toJSON():String {
        if(this.hasChildren()){
            return """{"id": $id, "name": "$name", "value": $decimalValue, "displayValue": "$textValue", "decimalPrecision": $decimalPrecision, "desc": "$desc", "hasSubAccounts": $hasSubAccounts, "hasSuperAccounts": $hasSuperAccounts, "localAccountID": $localAccountID, "subAccounts": """ +
                    CollectionToString.mkJSON(subAccounts as Iterable<JSONable>, ",\n")  + "}"
        }

        return """{"id": $id, "name": "$name", "value": $decimalValue, "displayValue": "$textValue", "decimalPrecision": $decimalPrecision, "desc": "$desc", "hasSubAccounts": $hasSubAccounts, "hasSuperAccounts": $hasSuperAccounts, "localAccountID":$localAccountID, "scalar": ${unit.scalar}, "isCurrency": ${unit is CurrencyUnit}, "isPercentage": ${unit is PercentageUnit}}"""
    }

    class Builder(var id: Int? = null,
                  var name: String? = null,
                  var subAccounts: MutableSet<ProtoAccount>? = null,
                  var decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION,
                  var value: Long? = null,
                  var unit: ProtoUnit = CurrencyUnit(),
                  var desc: String = "",
                  var timeParameters: TimeParameters? = null,
                  var entity: ProtoEntity? = null,
                  var isStatistical: Boolean = false
    ) {
        var type: Int = 0

        /**
         *  for the atomic account, specify the value
         */
        constructor(id: Int, name: String, value: Long, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION, unit: ProtoUnit = CurrencyUnit(), desc: String = "", timeParameters: TimeParameters? = null, entity: ProtoEntity? = null, isStatistical: Boolean = false) :
                this(id, name, null, decimalPrecision = decimalPrecision, value = value, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical)

        /**
         *  for the aggregate account, specify subAccounts
         *  if ignore the parameter subAccounts, empty set by default
         */
        constructor(id: Int, name: String, subAccounts: MutableSet<out ProtoAccount>, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION, unit: ProtoUnit = CurrencyUnit(), desc: String = "", timeParameters: TimeParameters? = null, entity: ProtoEntity? = null, isStatistical: Boolean = false) :
                this(id, name, subAccounts = subAccounts as MutableSet<ProtoAccount>, decimalPrecision = decimalPrecision, value = null, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical)

        constructor(id: Int, name: String, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION, unit: ProtoUnit = CurrencyUnit(), desc: String = "", timeParameters: TimeParameters? = null, entity: ProtoEntity? = null, isStatistical: Boolean = false) :
                this(id, name, subAccounts = mutableSetOf(), decimalPrecision = decimalPrecision, value = null, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical)


        fun setType(type: Int): Builder {
            this.type = type
            return this
        }


        fun setValue(v:Long):Builder{
            if (type == VALUE_SETTER_AGGREGATE)
                throw Exception("method 'setValue' for atomic account can not be evoked for the aggregate account.")

            this.value = v
            this.subAccounts = null
            type = VALUE_SETTER_BASIC
            isStatistical = false
            return this
        }

        fun setValue(v:Double, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION):Builder{
            if (type == VALUE_SETTER_AGGREGATE)
                throw Exception("method 'setValue' for atomic account can not be evoked for the aggregate account.")

            this.decimalPrecision = decimalPrecision
            this.value = (v * Math.pow(10.0, decimalPrecision.toDouble())).toLong()
            this.subAccounts = null
            this.isStatistical = false
            this.type = VALUE_SETTER_EXTERNAL
            return this
        }

        fun setValue(subAccounts: MutableSet<ProtoAccount>): Builder {
            if (type != VALUE_SETTER_UNDETERMINED && type != VALUE_SETTER_AGGREGATE)
                throw Exception("method 'setValue' for aggregate account can not be evoked for the atomic account.")

            this.subAccounts = subAccounts
            this.value = null
            type = VALUE_SETTER_AGGREGATE
            return this
        }

        fun setBasicInfo(id: Int, name: String):Builder{
            this.id = id
            this.name = name
            return this
        }


        fun setDecimalPrecision(decimalPrecision: Int):Builder{
            this.decimalPrecision = decimalPrecision
            return this
        }

        fun setDesc(desc: String):Builder{
            this.desc = desc
            return this
        }

        fun setUnit(unit: ProtoUnit):Builder{
            this.unit = unit
            return this
        }

        fun setTimeParameters(timeParameters: TimeParameters):Builder{
            this.timeParameters = timeParameters
            return this
        }

        fun setEntity(entity: ProtoEntity):Builder{
            this.entity = entity
            return this
        }

        fun setStatistical(isStatistical: Boolean): Builder {
            if (!(type == VALUE_SETTER_BASIC || type == VALUE_SETTER_EXTERNAL))
                this.isStatistical = isStatistical

            return this
        }

        fun build():ProtoAccount{
            return when{
                type == VALUE_SETTER_BASIC || type == VALUE_SETTER_EXTERNAL -> ProtoAccount(id!!, name!!, null, decimalPrecision = decimalPrecision, value = value, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical)
                type == VALUE_SETTER_AGGREGATE -> ProtoAccount(id!!, name!!, subAccounts = subAccounts, decimalPrecision = decimalPrecision, value = null, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical)
                else -> throw Exception("Please evoke setValue at first!")
            }
        }

        companion object {
            val VALUE_SETTER_BASIC = 1
            val VALUE_SETTER_EXTERNAL = 2
            val VALUE_SETTER_AGGREGATE = 3
            val VALUE_SETTER_UNDETERMINED = 0
        }

    }

    companion object {
        fun builder():Builder{
            return Builder()
        }

        fun builder(template: ProtoAccount): Builder {
            with(template) {
                if (isAggregate) {
                    return Builder(id, name, subAccounts!!, decimalPrecision, unit, desc, timeParameters, entity, isStatistical).setType(VALUE_SETTER_AGGREGATE)
                } else {
                    return Builder(id, name, value!!, decimalPrecision, unit, desc, timeParameters, entity, isStatistical).setType(VALUE_SETTER_BASIC)
                }
            }
        }
    }
}