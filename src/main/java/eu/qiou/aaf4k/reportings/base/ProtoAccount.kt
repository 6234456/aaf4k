package eu.qiou.aaf4k.reportings.base

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.roundUpTo
import eu.qiou.aaf4k.util.strings.CollectionToString
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit
import java.time.LocalDate

interface ProtoAccount : JSONable, Identifiable {
    override val id: Long
    val name: String
    val decimalPrecision: Int
    var value: Long
    val desc: String
    val unit: ProtoUnit
    val superAccounts: MutableList<ProtoCollectionAccount>
    val timeParameters: TimeParameters?
    val entity: Entity?
    val isStatistical: Boolean
    val validateUntil: LocalDate?

    val isValidate: Boolean
        get() = validateUntil == null || (timeParameters == null) || (timeParameters as TimeParameters).end <= validateUntil

    val reportingType: ReportingType
        get() = ReportingType.AUTO

    val reportingValue: Double
        get() = when (reportingType) {
            ReportingType.AUTO -> Math.abs(displayValue)
            else -> displayValue * reportingType.sign
        }

    val reportingSide: ReportingSide
        get() = when (reportingType) {
            ReportingType.AUTO -> if (displayValue.equals(0.0) or (reportingValue / displayValue > 0)) ReportingSide.DEBTOR else ReportingSide.CREDITOR
            else -> if (reportingType.sign == 1) ReportingSide.DEBTOR else ReportingSide.CREDITOR
        }

    val isAggregate: Boolean
        get() = this is ProtoCollectionAccount

    val decimalValue: Double
        get() {
            return if (this is ProtoCollectionAccount)
                subAccounts.fold(0.0) { acc, e ->
                    acc + if (e.isStatistical) 0.0 else e.decimalValue
                }
            else {
                val tmp = value.toDouble() / Math.pow(10.0, decimalPrecision.toDouble())
                when (unit) {
                    is CurrencyUnit -> (unit as CurrencyUnit).convertFxTo(GlobalConfiguration.DEFAULT_CURRENCY_UNIT, timeParameters)(tmp)
                    else -> tmp
                }.roundUpTo(decimalPrecision)
            }
        }

    val displayUnit: ProtoUnit
    val displayValue: Double
        get() = when (unit) {
            is CurrencyUnit -> (unit as CurrencyUnit).convertFxTo(displayUnit, timeParameters)(decimalValue)
            else -> unit.convertTo(displayUnit)(decimalValue)
        }.roundUpTo(decimalPrecision)

    val textValue: String
        get() = displayUnit.format()(displayValue)

    // symbol for the collection account
    private fun superAccountStr(): String {
        return "[$id $name] ${if (reportingType != ReportingType.AUTO) reportingType.code else ""}"
    }

    private fun subAccountStr(): String {
        val tmp = if (reportingType != ReportingType.AUTO) reportingType.code else ""
        return if (isStatistical)
            "{$id $name} $tmp : $textValue"
        else
            "($id $name) $tmp : $textValue"
    }

    fun deepCopy(): ProtoAccount

    fun copyWith(value: Double, decimalPrecision: Int): ProtoAccount

    fun copyWith(value: Double): ProtoAccount {
        return copyWith(value, decimalPrecision)
    }

    // change in-place
    fun update(data: Map<Long, Double>, updateMethod: (Double, Double) -> Double = { valueNew, valueOld -> valueNew + valueOld }):ProtoAccount {
        if (this is ProtoCollectionAccount) {
            getChildren().forEach { it.update(data, updateMethod) }
        } else {
            data[id]?.let {
                value = Math.round(updateMethod(it, decimalValue) * Math.pow(10.0, decimalPrecision.toDouble()))
            }
        }

        return this
    }

    fun toStrings(lvl: Int = 0): String {
        if (this is ProtoCollectionAccount && getChildren().isNotEmpty()) {
            return CollectionToString.structuredToStr<ProtoCollectionAccount, ProtoAccount>(this, lvl, ProtoAccount::toStrings, ProtoAccount::superAccountStr)
        }

        return subAccountStr()
    }

    override fun toJSON(): String {
        if (this is ProtoCollectionAccount) {
            return """{"id": $id, "name": "$name", "value": $decimalValue, "displayValue": "$textValue", "decimalPrecision": $decimalPrecision, "desc": "$desc", "hasSubAccounts": true, "hasSuperAccounts": ${this.hasParent()}, "isStatistical": $isStatistical, "validateUntil":${if (validateUntil == null) "null" else "'$validateUntil'"}, "reportingType": "${reportingType.code}", "subAccounts": """ +
                    CollectionToString.mkJSON(subAccounts as Iterable<JSONable>, ",\n") + "}"
        }

        return """{"id": $id, "name": "$name", "value": $decimalValue, "displayValue": "$textValue", "decimalPrecision": $decimalPrecision, "desc": "$desc", "hasSubAccounts": false, "hasSuperAccounts": ${this.superAccounts.size > 0}, "isStatistical": $isStatistical, "validateUntil":${if (validateUntil == null) "null" else "'$validateUntil'"}, "scalar": ${unit.scalar}, "reportingType": "${reportingType.code}"}"""

    }

    fun shorten(whiteList: Iterable<Long>? = null, blackList: Iterable<Long>? = null): ProtoAccount {
        return if (this is ProtoCollectionAccount) {
            (if (whiteList != null) {
                this.sortedList().filter { !whiteList.contains(it.id) }
            } else {
                blackList?.map { this.search(it) } ?: this.sortedList().filter { it.value == 0L }
            }).fold(this.deepCopy() as ProtoCollectionAccount) { acc, x ->
                if (x != null) acc.removeRecursively(x) else acc
            }
        } else
            this.deepCopy()
    }

    fun notStatistical(): List<ProtoAccount> {
        if (!isStatistical) {
            if (this is ProtoCollectionAccount) {
                return subAccounts.fold(listOf()) { acc, protoAccount ->
                    acc + protoAccount.notStatistical()
                }
            }

            return listOf(this)
        }

        return listOf()
    }

    fun nullify(): ProtoAccount

}