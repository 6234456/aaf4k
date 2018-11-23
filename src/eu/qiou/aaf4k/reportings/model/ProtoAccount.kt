package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.reportings.model.ProtoAccount.Builder.Companion.VALUE_SETTER_AGGREGATE
import eu.qiou.aaf4k.reportings.model.ProtoAccount.Builder.Companion.VALUE_SETTER_BASIC
import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.roundUpTo
import eu.qiou.aaf4k.util.strings.CollectionToString
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.PercentageUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit
import java.time.LocalDate


/**
 * @author Qiou Yang
 * @since 1.0.0
 * @param id the internal id of the account
 * @param name the internal name of the account
 * @param unit specify the unit stored in the value
 *
 * @param isStatistical for aggregate account, will not be checked for duplicity
 */

//TODO simplify the base class only the necessary attributes   the foreign exchange into separate adaptor   ProtoAccount as Interface
open class ProtoAccount(val id: Long, open val name: String,
                        val subAccounts: MutableList<out ProtoAccount>? = null,
                        val decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION,
                        val value: Long? = null,
                        val unit: ProtoUnit = CurrencyUnit(),
                        val desc: String = "",
                        val timeParameters: TimeParameters? = null,
                        val entity: ProtoEntity? = null,
                        val isStatistical: Boolean = false,
                        val validateUntil: LocalDate? = null
                        ): Comparable<ProtoAccount>, JSONable, Drilldownable{

    /**
     *  for the atomic account, specify the value
     */
    constructor(id: Long, name: String, value: Long, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION, unit: ProtoUnit = CurrencyUnit(), desc: String = "", timeParameters: TimeParameters? = null, entity: ProtoEntity? = null, isStatistical: Boolean = false, validateUntil: LocalDate? = null) :
            this(id, name, null, decimalPrecision = decimalPrecision, value = value, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical, validateUntil = validateUntil)

    /**
     *  for the aggregate account, specify subAccounts
     *  if ignore the parameter subAccounts, empty set by default
     */
    constructor(id: Long, name: String, subAccounts: MutableList<ProtoAccount>, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION, unit: ProtoUnit = CurrencyUnit(), desc: String = "", timeParameters: TimeParameters? = null, entity: ProtoEntity? = null, isStatistical: Boolean = false, validateUntil: LocalDate? = null) :
            this(id, name, subAccounts = subAccounts, decimalPrecision = decimalPrecision, value = null, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical, validateUntil = validateUntil)

    constructor(id: Long, name: String, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION, unit: ProtoUnit = CurrencyUnit(), desc: String = "", timeParameters: TimeParameters? = null, entity: ProtoEntity? = null, isStatistical: Boolean = false, validateUntil: LocalDate? = null) :
            this(id, name, subAccounts = mutableListOf(), decimalPrecision = decimalPrecision, value = null, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical, validateUntil = validateUntil)

    init {
        if (value == null && subAccounts == null)
            throw Exception("either value or subAccounts should be specified.")

        if (value != null && subAccounts != null)
            throw Exception("Only one of the parameters, value or subAccounts, should be specified.")

        subAccounts?.let {
            it.forEach { it.register(this) }
        }
    }

    override fun add(child: Drilldownable, index: Int?): Drilldownable {
        if (isAggregate) {
            if(child is ProtoAccount){
                child.register(this)
                with(subAccounts!! as MutableList<ProtoAccount>) {
                    if (index == null)
                        add(child)
                    else
                        add(index, child)
                }
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

    var superAccounts: MutableList<ProtoAccount>? = null
    var hasSuperAccounts: Boolean = false
    get() = hasParent()
    private set

    // can be re-implemented based on Locale
    var localAccountID: Long = id
    var localAccountName: String = id.toString()

    val isAggregate:Boolean = subAccounts != null

    var decimalValue: Double = 0.0
        get() {
            return if (isAggregate)
                subAccounts!!.fold(0.0) { acc, e ->
                    acc + if (e.isStatistical) 0.0 else e.decimalValue
                }
            else
                value!!.toDouble() / Math.pow(10.0, decimalPrecision.toDouble())
        }
        private set

    var displayUnit: ProtoUnit = CurrencyUnit()
    var displayValue: Double = 0.0
        get() = when {
            this.unit is CurrencyUnit -> unit.convertFxTo(displayUnit, timeParameters)(decimalValue)
            else -> unit.convertTo(displayUnit)(decimalValue)
        }.roundUpTo(decimalPrecision)
        private set


    var textValue: String? = displayUnit.format()(displayValue)
        get() = displayUnit.format()(displayValue)
        private set


    fun register(superAccount: ProtoAccount){
        if(superAccounts == null){
            superAccounts = mutableListOf(superAccount)
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

    //TODO: can be optimized
    fun shorten(whiteList: Iterable<ProtoAccount>? = null, blackList: Iterable<ProtoAccount>? = null): ProtoAccount {

        val e = if (whiteList != null) {
            (this.flatten() as List<ProtoAccount>).filter { !whiteList.contains(it) }
        } else {
            blackList ?: (this.flatten() as List<ProtoAccount>).filter { it.value == 0L }
        }

        return e.fold(this.deepCopy { it }) { acc, e ->
            acc.removeRecursively(e) as ProtoAccount
        }
    }

    fun notStatistical(): List<ProtoAccount> {
        if (!isStatistical) {
            if (isAggregate) {
                return subAccounts!!.fold(listOf<ProtoAccount>()) { acc, protoAccount ->
                    acc + protoAccount.notStatistical()
                }
            }

            return listOf(this)
        }

        return listOf()
    }

    fun nullify(): ProtoAccount {
        return this.deepCopy { it.toBuilder().setValue(0).build() }
    }

    open fun findChildByID(id: Long): ProtoAccount? {
        if (this.id == id) {
            return this
        }

        subAccounts?.forEach {
            it.findChildByID(id)?.let {
                return it
            }
        }

        return null
    }

    fun toBuilder(): Builder {
        return builder(this)
    }


    // the general method to transform a ProtoAccount
    open fun <T : ProtoAccount> deepCopy(callbackAtomicAccount: (T) -> T): T {
        if (this.isAggregate) {
            return this.toBuilder().setType(VALUE_SETTER_AGGREGATE).setValue(
                    this.subAccounts!!.map { it.deepCopy<T>(callbackAtomicAccount) }.toMutableList()
            ).build() as T
        } else {
            return callbackAtomicAccount(this as T)
        }
    }

    open fun <T : ProtoAccount> deepCopy(data: Map<Long, Double>, updateMethod: (Double, Double) -> Double = { valueNew, _ -> valueNew }): T {
        val callback: (ProtoAccount) -> ProtoAccount = {
            it.update(data, updateMethod)
        }
        return deepCopy(callback) as T
    }

    fun <T : ProtoAccount> deepCopy(entry: ProtoEntry<T>, updateMethod: (Double, Double) -> Double = { valueNew, _ -> valueNew }): T {
        return deepCopy(entry.toDataMap(), updateMethod)
    }

    fun <T : ProtoAccount> deepCopy(category: ProtoCategory<T>, updateMethod: (Double, Double) -> Double = { valueNew, _ -> valueNew }): T {
        return deepCopy(category.toDataMap(), updateMethod)
    }

    fun update(map: Map<Long, Double>, callback: (Double, Double) -> Double = { valueNew, _ -> valueNew }): ProtoAccount {
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

    fun titel(): String {
        return "[$id $name]"
    }

    @Suppress("UNCHECKED_CAST")
    override fun toString(): String {
        if (this.hasChildren()){
            return CollectionToString.structuredToStr(this, 0, ProtoAccount::toString as Drilldownable.() -> String, ProtoAccount::titel as Drilldownable.() -> String)
        }

        return if (isStatistical) "{$localAccountID $name} : $textValue" else "($localAccountID $name) : $textValue"
    }

    override fun toJSON():String {
        if(this.hasChildren()){
            return """{"id": $id, "name": "$name", "value": $decimalValue, "displayValue": "$textValue", "decimalPrecision": $decimalPrecision, "desc": "$desc", "hasSubAccounts": $hasSubAccounts, "hasSuperAccounts": $hasSuperAccounts, "localAccountID": $localAccountID, "isStatistical": $isStatistical, "validateUntil":${if (validateUntil == null) "null" else "'$validateUntil'"}, "subAccounts": """ +
                    CollectionToString.mkJSON(subAccounts as Iterable<JSONable>, ",\n")  + "}"
        }

        return """{"id": $id, "name": "$name", "value": $decimalValue, "displayValue": "$textValue", "decimalPrecision": $decimalPrecision, "desc": "$desc", "hasSubAccounts": $hasSubAccounts, "hasSuperAccounts": $hasSuperAccounts, "localAccountID":$localAccountID, "isStatistical": $isStatistical, "validateUntil":${if (validateUntil == null) "null" else "'$validateUntil'"}, "scalar": ${unit.scalar}, "isCurrency": ${unit is CurrencyUnit}, "isPercentage": ${unit is PercentageUnit}}"""
    }

    class Builder(var id: Long? = null,
                  var name: String? = null,
                  var subAccounts: MutableList<ProtoAccount>? = null,
                  var decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION,
                  var value: Long? = null,
                  var unit: ProtoUnit = CurrencyUnit(),
                  var desc: String = "",
                  var timeParameters: TimeParameters? = null,
                  var entity: ProtoEntity? = null,
                  var isStatistical: Boolean = false,
                  var validateUntil: LocalDate? = null
    ) {
        var type: Int = 0
        var displayUnit: ProtoUnit = CurrencyUnit()
        var localAccountID: Long? = id
        var localAccountName: String? = name


        /**
         *  for the atomic account, specify the value
         */
        constructor(id: Long, name: String, value: Long, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION, unit: ProtoUnit = CurrencyUnit(), desc: String = "", timeParameters: TimeParameters? = null, entity: ProtoEntity? = null, isStatistical: Boolean = false, validateUntil: LocalDate? = null) :
                this(id, name, null, decimalPrecision = decimalPrecision, value = value, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical, validateUntil = validateUntil)

        /**
         *  for the aggregate account, specify subAccounts
         *  if ignore the parameter subAccounts, empty set by default
         */
        constructor(id: Long, name: String, subAccounts: MutableList<out ProtoAccount>, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION, unit: ProtoUnit = CurrencyUnit(), desc: String = "", timeParameters: TimeParameters? = null, entity: ProtoEntity? = null, isStatistical: Boolean = false, validateUntil: LocalDate? = null) :
                this(id, name, subAccounts = subAccounts as MutableList<ProtoAccount>, decimalPrecision = decimalPrecision, value = null, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical, validateUntil = validateUntil)

        constructor(id: Long, name: String, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION, unit: ProtoUnit = CurrencyUnit(), desc: String = "", timeParameters: TimeParameters? = null, entity: ProtoEntity? = null, isStatistical: Boolean = false, validateUntil: LocalDate? = null) :
                this(id, name, subAccounts = mutableListOf(), decimalPrecision = decimalPrecision, value = null, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical, validateUntil = validateUntil)

        fun setDisplayUnit(unit: ProtoUnit): Builder {
            this.displayUnit = unit
            return this
        }

        fun setValidateUntil(date: LocalDate?): Builder {
            this.validateUntil = date
            return this
        }


        fun setType(type: Int): Builder {
            this.type = type
            return this
        }


        fun setValue(v:Long):Builder{
            if (type == VALUE_SETTER_AGGREGATE)
                throw Exception("method 'setValue' for atomic account can not be evoked for the aggregate account. $id $name")

            this.value = v
            this.subAccounts = null
            type = VALUE_SETTER_BASIC
            return this
        }

        fun setValue(v:Double, decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION):Builder{
            if (type == VALUE_SETTER_AGGREGATE)
                throw Exception("method 'setValue' for atomic account can not be evoked for the aggregate account. $id $name")

            this.decimalPrecision = decimalPrecision
            //1212.34 * Math.pow(10.0,2.0) = 121233.99
            this.value = Math.round(v * Math.pow(10.0, decimalPrecision.toDouble()))
            this.subAccounts = null
            this.type = VALUE_SETTER_EXTERNAL
            return this
        }

        fun setValue(subAccounts: MutableList<out ProtoAccount>): Builder {
            if (type != VALUE_SETTER_UNDETERMINED && type != VALUE_SETTER_AGGREGATE)
                throw Exception("method 'setValue' for aggregate account can not be evoked for the atomic account.")

            this.subAccounts = subAccounts as MutableList<ProtoAccount>
            this.value = null
            type = VALUE_SETTER_AGGREGATE
            return this
        }

        fun setBasicInfo(id: Long, name: String): Builder {
            this.id = id
            this.name = name

            this.localAccountName = name
            this.localAccountID = id

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

        fun setLocalAccountID(id: Long): Builder {
            this.localAccountID = id

            return this
        }

        fun setLocalAccountName(name: String): Builder {
            this.localAccountName = name

            return this
        }

        fun build():ProtoAccount{
            return when{
                type == VALUE_SETTER_BASIC || type == VALUE_SETTER_EXTERNAL -> ProtoAccount(id!!, name!!, null, decimalPrecision = decimalPrecision, value = value, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical, validateUntil = validateUntil)
                type == VALUE_SETTER_AGGREGATE -> ProtoAccount(id!!, name!!, subAccounts = subAccounts, decimalPrecision = decimalPrecision, value = null, unit = unit, desc = desc, timeParameters = timeParameters, entity = entity, isStatistical = isStatistical, validateUntil = validateUntil)
                else -> throw Exception("Please evoke setValue at first!")
            }.apply {
                this.displayUnit = this@Builder.displayUnit
                this.localAccountID = this@Builder.localAccountID!!
                this.localAccountName = this@Builder.localAccountName!!
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
                return if (isAggregate) {
                    Builder(id, name, subAccounts!!, decimalPrecision, unit, desc, timeParameters, entity, isStatistical, validateUntil).setType(VALUE_SETTER_AGGREGATE)
                } else {
                    Builder(id, name, value!!, decimalPrecision, unit, desc, timeParameters, entity, isStatistical, validateUntil).setType(VALUE_SETTER_BASIC)
                }.setDisplayUnit(displayUnit).setLocalAccountID(localAccountID).setLocalAccountName(localAccountName)
            }
        }
    }
}