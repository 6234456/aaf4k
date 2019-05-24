package eu.qiou.aaf4k.plugins

import eu.qiou.aaf4k.util.foldTrackList
import eu.qiou.aaf4k.util.npv
import eu.qiou.aaf4k.util.replaceValueBasedOnIndex
import eu.qiou.aaf4k.util.roundUpTo
import eu.qiou.aaf4k.util.time.ofNext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * @param nominalRate the rate to discount the installments, not the annual rate
 * @param paymentPlan the payments
 * @param discountToBookingDate the date on which the asset is initially recognized
 *
 * the bookingDate can differ from the first term according to the payment plan
 */

class PurchaseByInstallment(val id: Int, val desc: String = "", val nominalRate: Double,
                            paymentPlan: Map<LocalDate, Double>,
                            discountToBookingDate: Double = 1 / (1 + nominalRate), val precision: Int = 2) {

    constructor(id: Int, desc: String, nominalValue: Double, nominalRate: Double, paymentBegins: LocalDate, paymentTerms: Int,
                paymentIntervalUnit: ChronoUnit = ChronoUnit.YEARS,
                paymentIntervalAmount: Int = 1, discountToBookingDate: Double = 1 / (1 + nominalRate),
                precision: Int = 2) : this(id, desc, nominalRate = nominalRate, discountToBookingDate = discountToBookingDate,
            precision = precision,
            paymentPlan = paymentBegins.ofNext(paymentTerms, paymentIntervalUnit, paymentIntervalAmount)
                    .map { it to nominalValue / paymentTerms }.toMap())

    val nominalValue = paymentPlan.values.reduce { acc, d -> acc + d }
    val terms = paymentPlan.size
    val realValue = (paymentPlan.values.npv(nominalRate, true) * discountToBookingDate).roundUpTo(precision)

    val carryingAmount = paymentPlan.let {
        val l = it.values.foldTrackList(realValue) { acc, e, _ ->
            (acc - (e - (acc * nominalRate))).roundUpTo(precision)
        }
        // make sure the carryingAmount reduce to 0 in the last period
        (
                if (l.size < 2) l
                else l.dropLast(2) + listOf(l[l.size - 2] - l.last(), 0.0)
                ).replaceValueBasedOnIndex(it)
    }
}