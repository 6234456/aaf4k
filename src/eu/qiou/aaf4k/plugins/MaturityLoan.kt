package eu.qiou.aaf4k.plugins

import eu.qiou.aaf4k.util.irr
import eu.qiou.aaf4k.util.roundUpTo
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

    val effectiveInterest = this.paymentPlan.let {
        val k = it.keys.sorted().get(0)
        it.mapValues { e -> e.key }
    }


}