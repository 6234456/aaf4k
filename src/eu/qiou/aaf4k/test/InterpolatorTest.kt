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
        val i = Interpolator(growth = 0.2, period = TimeSpan.forYear(2019), interval = ChronoUnit.MONTHS, numberOfInterval = 3L, interpolationBase = InterpolationBase.END, interpolationType = InterpolationType.ABSOLUTE, decimalPosition = 4)
        val p = i.parse()
        println(p(128.3623))
        println(p(128.3623).values.reduce { acc, d -> acc + d })
    }

    @Test
    fun parse1() {
        val i = Interpolator(growth = -0.2, period = TimeSpan.forYear(2019), interval = ChronoUnit.MONTHS, interpolationBase = InterpolationBase.START, interpolationType = InterpolationType.PROPORTIONAL, decimalPosition = 2)
        val p = i.parse()

        println(p(1000.0))

    }
}