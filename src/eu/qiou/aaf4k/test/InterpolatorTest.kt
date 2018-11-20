package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.accounting.plan.InterpolationBase
import eu.qiou.aaf4k.accounting.plan.InterpolationType
import eu.qiou.aaf4k.accounting.plan.Interpolator
import eu.qiou.aaf4k.util.time.TimeSpan
import org.junit.Test
import java.time.temporal.ChronoUnit

class InterpolatorTest {

    @Test
    fun parse() {
        val i = Interpolator(growth = 0.2, period = TimeSpan.forYear(2019), interval = ChronoUnit.MONTHS, numberOfInterval = 3L, interpolationBase = InterpolationBase.END, interpolationType = InterpolationType.ABSOLUTE)
        val p = i.parse()
        println(p(100.0))
        println(p(100.0).values.reduce { acc, d -> acc + d })
    }
}