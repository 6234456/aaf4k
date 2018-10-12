package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.model.Drilldownable
import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.reportings.model.ProtoEntity
import eu.qiou.aaf4k.util.roundUpTo
import eu.qiou.aaf4k.util.strings.CollectionToString
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit

class Account(id: Int, name: String,
              subAccounts: MutableSet<Account>? = null,
              decimalPrecision: Int = 2,
              value: Long? = null,
              unit: CurrencyUnit = CurrencyUnit(),
              val reportingType: ReportingType,
              desc: String = "",
              timeParameters: TimeParameters? = null,
              entity: ProtoEntity? = null,
              isStatistical: Boolean = false) : ProtoAccount(id, name, subAccounts, decimalPrecision, value, unit, desc, timeParameters, entity, isStatistical) {

    constructor(id: Int, name: String, decimalPrecision: Int, formula: String, accounts: Map<Int, Account>, unit: CurrencyUnit = CurrencyUnit(),
                reportingType: ReportingType, desc: String = "", timeParameters: TimeParameters? = null,
                entity: ProtoEntity? = null,
                isStatistical: Boolean = false) : this(id, name, null, decimalPrecision, parse(formula, accounts).roundUpTo(decimalPrecision).toLong(), unit, reportingType, desc, timeParameters, entity, isStatistical)
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

    override fun <T : ProtoAccount> deepCopy(data: Map<Int, Double>, updateMethod: (Double, Double) -> Double): T {
        return Account.from(super.deepCopy(data, updateMethod), this.reportingType) as T
    }

    companion object {
        fun from(protoAccount: ProtoAccount, reportingType: ReportingType): Account {
            return Account(id = protoAccount.id, name = protoAccount.name,
                    subAccounts = protoAccount.subAccounts as MutableSet<Account>?, decimalPrecision = protoAccount.decimalPrecision,
                    value = protoAccount.value, unit = protoAccount.unit as CurrencyUnit,
                    reportingType = reportingType, desc = protoAccount.desc, timeParameters = protoAccount.timeParameters,
                    entity = protoAccount.entity, isStatistical = protoAccount.isStatistical)
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
            return CollectionToString.structuredToStr(this, 0, ProtoAccount::toString as Drilldownable.() -> String, ProtoAccount::titel as Drilldownable.() -> String)
        }

        if (isStatistical)
            return "{$localAccountID $name ${reportingType.code}} : $textValue"
        else
            return "($localAccountID $name ${reportingType.code}) : $textValue"
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
    EQUITY(-1, "EQ"),
    LIABILITY(-1, "LB"),
    REVENUE_GAIN(-1, "RV"),
    EXPENSE_LOSS(1, "EP"),
    PROFIT_LOSS_NEUTRAL(-1, "NT"),
    ANNUAL_RESULT(-1, "RE"),
    AUTO(0, "NN")
}


enum class ReportingSide(val sign: Int) {
    DEBTOR(1),
    CREDITOR(-1)
}