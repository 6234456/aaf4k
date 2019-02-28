package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.accounting.plan.DepreciationPlan
import org.junit.Test
import java.time.LocalDate

class DepreciationPlanTest {

    @Test
    fun generate() {
        println(DepreciationPlan(1000.23, residualAbsoluteValue = 1.0, numberOfPeriods = 10).generate(LocalDate.now()))
    }
}