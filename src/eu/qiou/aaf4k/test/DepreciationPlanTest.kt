package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.accounting.plan.DepreciationMethod
import eu.qiou.aaf4k.accounting.plan.DepreciationPlan
import eu.qiou.aaf4k.util.roundUpTo
import org.junit.Test
import java.time.LocalDate

class DepreciationPlanTest {

    @Test
    fun generate() {
        val a = DepreciationPlan( residualPercentage = 0.2, numberOfPeriods = 5, method = DepreciationMethod.NUMBER_OF_TOTAL_YEARS).generate(10000.0, LocalDate.now())
        println(a)
        println(a.values.reduce { acc, d -> acc.roundUpTo(2) + d })
    }
}