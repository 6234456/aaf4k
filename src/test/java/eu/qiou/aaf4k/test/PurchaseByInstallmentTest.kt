package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.PurchaseByInstallment
import eu.qiou.aaf4k.util.withinTolerance
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PurchaseByInstallmentTest {


    @Test
    fun getNominalValue() {
        val installment = PurchaseByInstallment(1, "", nominalValue = 9000000.0, nominalRate = 0.1, paymentBegins =
        LocalDate.of(2015, 6, 30), paymentTerms = 10, paymentIntervalUnit = ChronoUnit.MONTHS, paymentIntervalAmount = 6, precision = 0)

        assert(installment.terms == 10)
        assert(installment.nominalValue == 9000000.0)
        assert(installment.realValue.withinTolerance(0.00001, 5530140))
        println(installment.carryingAmount)
    }

    @Test
    fun getTerms() {
        val installment = PurchaseByInstallment(1, "", nominalValue = 1000.0, nominalRate = 0.05, paymentBegins =
        LocalDate.of(2015, 12, 31), paymentTerms = 5, precision = 2)
        println(installment.carryingAmount)
        println(installment.realValue)

    }

    @Test
    fun getRealValue() {
    }

    @Test
    fun getCarryingAmount() {
    }

    @Test
    fun getId() {
    }

    @Test
    fun getDesc() {
    }

    @Test
    fun getNominalRate() {
    }

    @Test
    fun getPrecision() {
    }
}