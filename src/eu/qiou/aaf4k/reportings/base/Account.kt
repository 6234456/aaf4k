package eu.qiou.aaf4k.reportings.base

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit
import java.time.LocalDate

// the implementation of atomic account
data class Account(override val id: Long, override val name: String,
                   override var value: Long = 0L,
                   override val decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION,
                   override val unit: ProtoUnit = CurrencyUnit(decimalPrecision = decimalPrecision),
                   override val desc: String = "",
                   override val timeParameters: TimeParameters? = null,
                   override val entity: Entity? = null,
                   override val isStatistical: Boolean = false,
                   override val validateUntil: LocalDate? = null,
                   override val reportingType: ReportingType = ReportingType.AUTO, override val displayUnit: ProtoUnit = unit
) : ProtoAccount {

    companion object {
        private var reportingCodePrepared = false
        private lateinit var reportingCode: Map<String, ReportingType>

        val parseReportingType: (String) -> ReportingType = { x->
            if (!reportingCodePrepared) {
                reportingCode = ReportingType.values().map {
                    it.code to it
                }.toMap()

                reportingCodePrepared = true
            }
            reportingCode[x] ?: error("Undefined reporting code $x")
        }
    }

    override fun nullify(): ProtoAccount {
        return copy(value = 0L)
    }

    override val superAccounts: MutableList<ProtoCollectionAccount> = mutableListOf()

    override fun deepCopy(): ProtoAccount {
        return this.copy(id = this.id)
    }

    override fun copyWith(value: Double, decimalPrecision: Int): ProtoAccount {
        return this.copy(
                decimalPrecision = decimalPrecision,
                //1212.34 * Math.pow(10.0,2.0) = 121233.99
                value = Math.round(value * Math.pow(10.0, decimalPrecision.toDouble()))
        )
    }

    override fun toString(): String = toStrings()

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