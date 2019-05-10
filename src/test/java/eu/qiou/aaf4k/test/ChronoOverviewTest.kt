package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.plan.InterpolationBase
import eu.qiou.aaf4k.reportings.plan.Interpolator
import eu.qiou.aaf4k.util.template.ChronoOverview
import eu.qiou.aaf4k.util.time.TimeSpan
import java.util.*
import kotlin.test.Test

class ChronoOverviewTest{

    @Test
    fun trail(){
        Locale.setDefault(Locale.GERMAN)

        val timeSpan = TimeSpan.forYear(2018)

        val interpolator = Interpolator(growth = 0.1, period = timeSpan, interpolationBase = InterpolationBase.END).parse()

        ChronoOverview(timeSpan = timeSpan, data = mapOf(
                "account1" to interpolator(1000.0).values.toList(),
                "account2" to interpolator(1800.0).values.toList(),
                "account3" to interpolator(2000.0).values.toList()
        )).build("data/demo_chrono.xlsx")
    }
}