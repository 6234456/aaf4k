package eu.qiou.aaf4k.reportings.base

data class InterCompanyPolicy(
        val srcEntity: Entity,
        val targetEntity: Entity,
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

            else -> throw Exception("Cannot infer the PolicyType from ${account.reportingType}")
        },
        val method: ConsolidationMethod = ConsolidationMethod.FULL_CONSOLIDATION
) {
    companion object {
        fun eliminate(context: Map<Entity, Map<Entity, Map<InterCompanyPolicyType, List<InterCompanyPolicy>>>>, reporting: Reporting) {
            val diffSchuKons = reporting.diffSchuKons
                    ?: throw Exception("Difference Settling Account should be specified!")
            val diffAEKons = reporting.diffAEKons ?: throw Exception("Difference Settling Account should be specified!")

            if (!reporting.consCategoriesAdded) {
                throw Exception("Please invoke prepare consolidation first.")
            }

            val schuKonsCategory = (reporting.categories as List<Category>).find { it.consolidationCategory == ConsolidationCategory.PAYABLES_RECEIVABLES }!!
            val AEKonsCategory = (reporting.categories as List<Category>).find { it.consolidationCategory == ConsolidationCategory.REVENUE_EXPENSE }!!
            val visited: MutableMap<Entity, MutableList<Entity>> = mutableMapOf()


            context.forEach { k, v ->
                v.forEach { k1, v1 ->
                    v1.forEach { k2, v2 ->
                        if (context[k1]?.get(k)?.get(k2) != null) {
                            if (visited[k]?.contains(k1) != true && k1 != k) {
                                val c = when (k2) {
                                    InterCompanyPolicyType.RECEIVABLES_PAYABLES -> schuKonsCategory
                                    InterCompanyPolicyType.REVENUE_EXPENSE -> AEKonsCategory
                                }

                                val e = Entry("Auto-gen", c)

                                (v2 + context[k1]!![k]!![k2]!!).forEach {

                                    if (it.account.decimalValue != 0.0)
                                        e.add(it.account.id, it.account.decimalValue * -1)
                                }
                                e.balanceWith(when (k2) {
                                    InterCompanyPolicyType.RECEIVABLES_PAYABLES -> diffSchuKons.id
                                    InterCompanyPolicyType.REVENUE_EXPENSE -> diffAEKons.id
                                })

                                visited.putIfAbsent(k, mutableListOf())
                                visited[k]!!.add(k1)
                                visited.putIfAbsent(k1, mutableListOf())
                                visited[k1]!!.add(k)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class InterCompanyPolicyType {
    RECEIVABLES_PAYABLES,
    REVENUE_EXPENSE
}

enum class ConsolidationMethod {
    AT_EQUITY,
    FULL_CONSOLIDATION,
    QUOTE_CONSOLIDATION
}