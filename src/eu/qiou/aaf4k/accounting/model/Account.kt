package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.reportings.model.ProtoEntity
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

    val reportingValue: Double = when (reportingType) {
        ReportingType.AUTO -> Math.abs(displayValue)
        else -> displayValue * reportingType.sign
    }

    val reportingSide: ReportingSide = when (reportingType) {
        ReportingType.AUTO -> if (displayValue.equals(0.0) or (reportingValue / displayValue > 0)) ReportingSide.DEBTOR else ReportingSide.CREDITOR
        else -> if (reportingType.sign == 1) ReportingSide.DEBTOR else ReportingSide.CREDITOR
    }

    companion object {
        fun from(protoAccount: ProtoAccount, reportingType: ReportingType): Account {
            return Account(id = protoAccount.id, name = protoAccount.name,
                    subAccounts = protoAccount.subAccounts as MutableSet<Account>?, decimalPrecision = protoAccount.decimalPrecision,
                    value = protoAccount.value, unit = protoAccount.unit as CurrencyUnit,
                    reportingType = reportingType, desc = protoAccount.desc, timeParameters = protoAccount.timeParameters,
                    entity = protoAccount.entity, isStatistical = protoAccount.isStatistical)
        }
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