package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.reportings.model.ProtoEntity
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit

class Account(override val id: Int, override val name: String,
              override val subAccounts: MutableSet<Account>? = null,
              override val decimalPrecision: Int = 2,
              override val value: Long? = null,
              override val unit: CurrencyUnit = CurrencyUnit(),
              val reportingType: ReportingType,
              override val desc: String = "",
              override val timeParameters: TimeParameters? = null,
              override val entity: ProtoEntity? = null,
              override val isStatistical: Boolean = false) : ProtoAccount(id, name, decimalPrecision, unit, desc, timeParameters, entity, isStatistical) {

    constructor(protoAccount: ProtoAccount, reportingType: ReportingType) : this(protoAccount.id, protoAccount.name, protoAccount.subAccounts as MutableSet<Account>?, protoAccount.decimalPrecision, protoAccount.value, protoAccount.unit as CurrencyUnit, reportingType, protoAccount.desc, protoAccount.timeParameters, protoAccount.entity, protoAccount.isStatistical)

    val reportingValue: Double = when (reportingType) {
        ReportingType.AUTO -> Math.abs(displayValue)
        else -> displayValue * reportingType.sign
    }

    val reportingSide: ReportingSide = when (reportingType) {
        ReportingType.AUTO -> if (displayValue.equals(0.0) or (reportingValue / displayValue > 0)) ReportingSide.DEBTOR else ReportingSide.CREDITOR
        else -> if (reportingType.sign == 1) ReportingSide.DEBTOR else ReportingSide.CREDITOR
    }
}

/**
 *   @param sign * value = valueToDisplay
 *   AUTO: ReportingType depends on the value of account, like the VAT-Accounts / Verrechnungskonten in German-GAAP
 *   if the account set to ASSET, it will still on the active site even if the value is less than zero, just like the deprecation of the assets
 *   While as AUTO, it will reclassified to be display in positive value.
 */

enum class ReportingType(val sign: Int) {
    ASSET(1),
    EQUITY(-1),
    LIABILITY(-1),
    REVENUE_GAIN(-1),
    EXPENSE_LOSS(1),
    PROFIT_LOSS_NEUTRAL(-1),
    ANNUAL_RESULT(-1),
    AUTO(0)
}


enum class ReportingSide(val sign: Int) {
    DEBTOR(1),
    CREDITOR(-1)
}