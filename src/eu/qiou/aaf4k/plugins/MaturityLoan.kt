package eu.qiou.aaf4k.plugins

import eu.qiou.aaf4k.accounting.model.*
import eu.qiou.aaf4k.util.*
import eu.qiou.aaf4k.util.time.ofNext
import eu.qiou.aaf4k.util.time.times
import eu.qiou.aaf4k.util.time.to
import eu.qiou.aaf4k.util.time.toPercentageOfYear
import java.time.LocalDate
import java.time.temporal.ChronoUnit


class MaturityLoan(val id: Int, val desc: String = "", val nominalValue: Double, val realValue: Double, paymentPlan: Map<LocalDate, Double>, val precision: Int = 2) {

    constructor(id: Int, desc: String = "", nominalValue: Double, realValue: Double, nominalRate: Double, paymentBegins: LocalDate, paymentEnds: LocalDate, paymentIntervalUnit: ChronoUnit = ChronoUnit.YEARS, paymentIntervalAmount: Int = 1, releaseDate: LocalDate = paymentBegins - paymentIntervalUnit * paymentIntervalAmount, precision: Int = 2)
            : this(id = id, desc = desc, nominalValue = nominalValue, realValue = realValue,
            paymentPlan = mapOf<LocalDate, Double>(releaseDate to realValue) + paymentBegins.to(paymentEnds, paymentIntervalUnit, paymentIntervalAmount)
                    .let {
                        it.mapIndexed { index, localDate ->
                            localDate to
                                    -1.0 * (nominalRate * paymentIntervalUnit.toPercentageOfYear() * paymentIntervalAmount * nominalValue + (if (it.count() - 1 == index) nominalValue else 0.0))
                        }
                                .toMap()
                    }
            , precision = precision)


    constructor(id: Int, desc: String = "", nominalValue: Double, realValue: Double, nominalRate: Double, paymentBegins: LocalDate, paymentTerms: Int, paymentIntervalUnit: ChronoUnit = ChronoUnit.YEARS, paymentIntervalAmount: Int = 1,
                releaseDate: LocalDate = paymentBegins - paymentIntervalUnit * paymentIntervalAmount, precision: Int = 2)
            : this(id = id, desc = desc, nominalValue = nominalValue, realValue = realValue,
            paymentPlan = mapOf<LocalDate, Double>(releaseDate to realValue) + paymentBegins.ofNext(paymentTerms, paymentIntervalUnit, paymentIntervalAmount)
                    .let {
                        it.mapIndexed { index, localDate ->
                            localDate to
                                    -1.0 * (nominalRate * paymentIntervalUnit.toPercentageOfYear() * paymentIntervalAmount * nominalValue + (if (it.count() - 1 == index) nominalValue else 0.0))
                        }
                                .toMap()
                    },
            precision = precision)


    val paymentPlan = paymentPlan.mapValues { it.value.roundUpTo(precision) }

    val r = this.paymentPlan.values.irr()

    val carryingAmount = this.paymentPlan.let {
        it.values.reduceTrackList { acc, e, i ->
            if (i > 0) (acc * (1 + r) + e).roundUpTo(precision) else acc
        }.replaceValueBasedOnIndex(it)
    }

    val effectiveInterest = carryingAmount.mapValues {
        (it.value * r).roundUpTo(precision)
    }

    fun toEntries(): Map<LocalDate, Entry> {
        val keys = effectiveInterest.keys.toList()
        var cnt = 0
        val reporting = Reporting(0, "Demo Reporting", "Demo",
                listOf(
                        Account(0, "Langfristige Verbindlichkeit KI", subAccounts = mutableSetOf(
                                Account(3, "Verbindlichkeit KI - Principal", reportingType = ReportingType.LIABILITY, value = 0),
                                Account(4, "Verbindlichkeit KI - Accrued Interest", reportingType = ReportingType.LIABILITY, value = 0)
                        ), reportingType = ReportingType.LIABILITY),
                        Account(1, "Guthaben KI", value = 0, reportingType = ReportingType.ASSET),
                        Account(2, "Zinsaufwand", value = 0, reportingType = ReportingType.EXPENSE_LOSS)
                )
        )
        val category = Category("Maturity Loan", 0,
                "Entries Generated for Maturity Loan #${id}", reporting)

        return carryingAmount.mapValuesIndexed { v, i ->
            if (i == 0) {
                Entry(cnt++, "Initial Recognition", category).apply {
                    this.add(1, v.value)
                    this.add(3, nominalValue * -1)
                }.balanceWith(4)
            } else {
                Entry(cnt++, "${v.key}", category).apply {
                    this.add(2, effectiveInterest[keys[i - 1]]!!)
                    this.add(1, paymentPlan[keys[i]]!!)
                }.balanceWith(4)
            }
        }
    }
}