package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.MaturityLoan
import eu.qiou.aaf4k.util.irr
import eu.qiou.aaf4k.util.npv
import eu.qiou.aaf4k.util.roundUpTo
import eu.qiou.aaf4k.util.time.to
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class MaturityLoanTest {

    @Test
    fun getPaymentPlan() {
        println(LocalDate.of(1996, 2, 29).to(LocalDate.of(2018, 2, 28)))
        println(LocalDate.of(2016, 2, 29).to(LocalDate.of(2018, 2, 28)))
        println(LocalDate.of(2018, 1, 31).to(LocalDate.of(2018, 10, 31), ChronoUnit.MONTHS))

        println(MaturityLoan(1, "", 100.0, 80.0, 0.15, LocalDate.of(2016, 12, 31), LocalDate.of(2018, 12, 31), ChronoUnit.MONTHS, 3).r)
        println(MaturityLoan(1, "", 100.0, 80.0, 0.15, LocalDate.of(2016, 12, 31), LocalDate.of(2018, 12, 31), ChronoUnit.MONTHS, 3).effectiveInterest)
        println(MaturityLoan(1, "", 100.0, 80.0, 0.15, LocalDate.of(2016, 12, 31), 6, ChronoUnit.MONTHS, 3).paymentPlan)
    }

    @Test
    fun getPrecision() {
        println(listOf(80, -15, -15, -115).irr())
        println(listOf(80, -15, -15, -115).npv(0.252900564))
        println(listOf(80, -15, -15, -115).npv(0.252899538397789))
    }

    @Test
    fun effectiveIn() {
        val loan = MaturityLoan(1, "", 215440.0, 15440.0, 0.047,
                LocalDate.of(2001, 1, 1),
                LocalDate.of(2005, 12, 31),
                ChronoUnit.YEARS, 1, precision = 0)

        with(loan) {
            println(r)
            println(paymentPlan)
            println(paymentPlan.values.npv(r).roundUpTo(precision))
            println(carryingAmount)
            println(effectiveInterest)
            println(toEntries())
        }
    }
}