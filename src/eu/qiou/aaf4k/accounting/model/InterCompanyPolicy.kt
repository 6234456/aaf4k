package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.model.ProtoEntity

data class InterCompanyPolicy(
        val srcEntity: ProtoEntity,
        val targetEntity: ProtoEntity,
        val account: Account,
        val type: InterCompanyPolicyType = when (account.reportingType) {
            ReportingType.ASSET,
            ReportingType.ASSET_SHORT_TERM,
            ReportingType.ASSET_LONG_TERM,
            ReportingType.LIABILITY,
            ReportingType.LIABILITY_LONG_TERM,
            ReportingType.LIABILITY_SHORT_TERM
            -> InterCompanyPolicyType.RECEIVABLES_PAYABLES

            ReportingType.EXPENSE_LOSS, ReportingType.REVENUE_GAIN, ReportingType.PROFIT_LOSS_NEUTRAL
            -> InterCompanyPolicyType.REVENUE_EXPENSE

            else -> throw Exception("Cannot defer the PolicyType from ${account.reportingType}")
        }
)

enum class InterCompanyPolicyType {
    RECEIVABLES_PAYABLES,
    REVENUE_EXPENSE
}

enum class ConsolidationMethod {
    AT_EQUITY,
    FULL_CONSOLIDATION,
    QUOTE_CONSOLIDATION
}