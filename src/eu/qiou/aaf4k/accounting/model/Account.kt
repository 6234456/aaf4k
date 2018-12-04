package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.model.Drilldownable
import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.reportings.model.ProtoEntity
import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.roundUpTo
import eu.qiou.aaf4k.util.strings.CollectionToString
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import java.time.LocalDate

class Account(id: Long, name: String,
              subAccounts: MutableList<Account>? = null,
              decimalPrecision: Int = 2,
              value: Long? = null,
              unit: CurrencyUnit = CurrencyUnit(),
              val reportingType: ReportingType,
              desc: String = "",
              timeParameters: TimeParameters? = null,
              entity: ProtoEntity? = null,
              isStatistical: Boolean = false,
              validateUntil: LocalDate? = null
) : ProtoAccount(id, name, subAccounts, decimalPrecision, value, unit, desc, timeParameters, entity, isStatistical, validateUntil) {

    constructor(id: Long, name: String, decimalPrecision: Int, formula: String, accounts: Map<Int, Account>, unit: CurrencyUnit = CurrencyUnit(),
                reportingType: ReportingType, desc: String = "", timeParameters: TimeParameters? = null,
                entity: ProtoEntity? = null,
                isStatistical: Boolean = false, validateUntil: LocalDate? = null) : this(id, name, null, decimalPrecision, parse(formula, accounts).roundUpTo(decimalPrecision).toLong(), unit, reportingType, desc, timeParameters, entity, isStatistical, validateUntil)
    val reportingValue: Double = when (reportingType) {
        ReportingType.AUTO -> Math.abs(displayValue)
        else -> displayValue * reportingType.sign
    }

    val reportingSide: ReportingSide = when (reportingType) {
        ReportingType.AUTO -> if (displayValue.equals(0.0) or (reportingValue / displayValue > 0)) ReportingSide.DEBTOR else ReportingSide.CREDITOR
        else -> if (reportingType.sign == 1) ReportingSide.DEBTOR else ReportingSide.CREDITOR
    }

    override fun <T : ProtoAccount> deepCopy(callbackAtomicAccount: (T) -> T): T {
        return Account.from(super.deepCopy(callbackAtomicAccount), this.reportingType) as T
    }

    override fun <T : ProtoAccount> deepCopy(data: Map<Long, Double>, updateMethod: (Double, Double) -> Double): T {
        return Account.from(super.deepCopy(data, updateMethod), this.reportingType) as T
    }

    companion object {
        fun from(protoAccount: ProtoAccount, reportingType: ReportingType): Account {
            return Account(id = protoAccount.id, name = protoAccount.name,
                    subAccounts = protoAccount.subAccounts as MutableList<Account>?, decimalPrecision = protoAccount.decimalPrecision,
                    value = protoAccount.value, unit = protoAccount.unit as CurrencyUnit,
                    reportingType = reportingType, desc = protoAccount.desc, timeParameters = protoAccount.timeParameters,
                    entity = protoAccount.entity, isStatistical = protoAccount.isStatistical, validateUntil = protoAccount.validateUntil).apply {
                this.displayUnit = protoAccount.displayUnit
                this.localAccountName = protoAccount.localAccountName
                this.localAccountID = protoAccount.localAccountID
            }
        }

        val parseReportingType: (String) -> ReportingType = {
            when (it) {
                ReportingType.ASSET.code -> ReportingType.ASSET
                ReportingType.ASSET_LONG_TERM.code -> ReportingType.ASSET_LONG_TERM
                ReportingType.ASSET_SHORT_TERM.code -> ReportingType.ASSET_SHORT_TERM
                ReportingType.EQUITY.code -> ReportingType.EQUITY
                ReportingType.LIABILITY.code -> ReportingType.LIABILITY
                ReportingType.LIABILITY_LONG_TERM.code -> ReportingType.LIABILITY_LONG_TERM
                ReportingType.LIABILITY_SHORT_TERM.code -> ReportingType.LIABILITY_SHORT_TERM
                ReportingType.REVENUE_GAIN.code -> ReportingType.REVENUE_GAIN
                ReportingType.EXPENSE_LOSS.code -> ReportingType.EXPENSE_LOSS
                ReportingType.PROFIT_LOSS_NEUTRAL.code -> ReportingType.PROFIT_LOSS_NEUTRAL
                ReportingType.PROFIT_LOSS_NEUTRAL_BALANCE.code -> ReportingType.PROFIT_LOSS_NEUTRAL_BALANCE
                ReportingType.RESULT_BALANCE.code -> ReportingType.RESULT_BALANCE
                ReportingType.RETAINED_EARNINGS_BEGINNING.code -> ReportingType.RETAINED_EARNINGS_BEGINNING
                ReportingType.AUTO.code -> ReportingType.AUTO
                ReportingType.DIFF_CONS_RECEIVABLE_PAYABLE.code -> ReportingType.DIFF_CONS_RECEIVABLE_PAYABLE
                ReportingType.DIFF_CONS_REVENUE_EXPENSE.code -> ReportingType.DIFF_CONS_REVENUE_EXPENSE
                else -> throw java.lang.Exception("ParameterError: unknown ReportingType:$it")
            }
        }

        fun parse(formula: String, accounts: Map<Int, Account>): Double {
            return 0.0
        }
    }

    operator fun plus(account: Account): Double = this.decimalValue + account.decimalValue
    operator fun minus(account: Account): Double = this.decimalValue - account.decimalValue
    operator fun plus(v: Double): Double = this.decimalValue + v
    operator fun minus(v: Double): Double = this.decimalValue - v
    operator fun times(v: Double): Double = this.decimalValue * v


    @Suppress("UNCHECKED_CAST")
    override fun toString(): String {
        if (this.hasChildren()) {
            return CollectionToString.structuredToStr(this, 0, Account::toString as Drilldownable.() -> String, Account::titel as Drilldownable.() -> String)
        }

        if (isStatistical)
            return "{$localAccountID $name ${reportingType.code}} : $textValue"
        else
            return "($localAccountID $name ${reportingType.code}) : $textValue"
    }

    override fun toJSON(): String {
        if (this.hasChildren()) {
            return """{"id": $id, "name": "$name", "value": $decimalValue, "displayValue": "$textValue", "decimalPrecision": $decimalPrecision, "desc": "$desc", "hasSubAccounts": $hasSubAccounts, "hasSuperAccounts": $hasSuperAccounts, "localAccountID": $localAccountID, "isStatistical": $isStatistical, "validateUntil":${if (validateUntil == null) "null" else "'$validateUntil'"}, "reportingType": "${reportingType.code}", "subAccounts": """ +
                    CollectionToString.mkJSON(subAccounts as Iterable<JSONable>, ",\n") + "}"
        }

        return """{"id": $id, "name": "$name", "value": $decimalValue, "displayValue": "$textValue", "decimalPrecision": $decimalPrecision, "desc": "$desc", "hasSubAccounts": $hasSubAccounts, "hasSuperAccounts": $hasSuperAccounts, "localAccountID":$localAccountID, "isStatistical": $isStatistical, "validateUntil":${if (validateUntil == null) "null" else "'$validateUntil'"}, "scalar": ${unit.scalar}, "reportingType": "${reportingType.code}"}"""

    }


}

/**
 *   @param sign * value = valueToDisplay
 *   AUTO: ReportingType depends on the value of account, like the VAT-Accounts / Verrechnungskonten in German-GAAP
 *   if the account set to ASSET, it will still on the active site even if the value is less than zero, just like the deprecation of the assets
 *   While as AUTO, it will reclassified to be display in positive value.
 */

enum class ReportingType(val sign: Int, val code: String) {
    ASSET(1, "AS"),
    ASSET_SHORT_TERM(1, "AK"),
    ASSET_LONG_TERM(1, "AL"),
    EQUITY(-1, "EQ"),
    LIABILITY(-1, "LB"),
    LIABILITY_SHORT_TERM(-1, "LK"),
    LIABILITY_LONG_TERM(-1, "LL"),
    REVENUE_GAIN(-1, "RV"),
    EXPENSE_LOSS(1, "EP"),
    PROFIT_LOSS_NEUTRAL(-1, "NT"),
    PROFIT_LOSS_NEUTRAL_BALANCE(-1, "OC"),
    RESULT_BALANCE(-1, "RE"),
    RETAINED_EARNINGS_BEGINNING(-1, "RT"),
    DIFF_CONS_RECEIVABLE_PAYABLE(1, "KP"),
    DIFF_CONS_REVENUE_EXPENSE(-1, "KR"),
    AUTO(0, "NN")
}


enum class ReportingSide(val sign: Int) {
    DEBTOR(1),
    CREDITOR(-1)
}