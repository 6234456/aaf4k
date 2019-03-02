package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.accounting.plan.DepreciationPlan
import eu.qiou.aaf4k.util.roundUpTo
import org.junit.Test
import java.time.LocalDate

class DepreciationPlanTest {

    @Test
    fun generate() {
        val a = DepreciationPlan.EstG_7_5_1_3.generate(1000000.25, LocalDate.now())
        println(a)
        println(a.values.reduce { acc, d -> acc.roundUpTo(2) + d })
    }
}