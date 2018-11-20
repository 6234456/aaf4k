package eu.qiou.aaf4k.accounting.plan

import eu.qiou.aaf4k.util.time.TimeSpan
import java.time.temporal.ChronoUnit

/**
 * @param interpolationBase START : set the base to Dec. END:  with target for the whole year divided through periods
 * @param interpolationType  whether the growth is proportional or absolute
 * @param growth    the growth
 * @param period    the plan period
 * @param interval  the intervals in the period
 */

class Interpolator(val interpolationBase: InterpolationBase = InterpolationBase.START, val interpolationType: InterpolationType = InterpolationType.PROPORTIONAL, val growth: Double, val period: TimeSpan, val interval: ChronoUnit = ChronoUnit.MONTHS, val numberOfInterval: Long = 1) {
    private val periods = period.drillDown(numberOfInterval, interval)

    fun parse(): (Double) -> Map<TimeSpan, Double> {
        return when (interpolationBase) {
            InterpolationBase.START -> { x ->
                periods.mapIndexed { index, timeSpan ->
                    timeSpan to
                            when (interpolationType) {
                                InterpolationType.PROPORTIONAL -> x * Math.pow(1 + growth, index.toDouble() + 1)
                                InterpolationType.ABSOLUTE -> x + growth * (index.toDouble() + 1)
                            }
                }.toMap()
            }
            InterpolationBase.END -> { x ->
                when (interpolationType) {
                    InterpolationType.PROPORTIONAL -> {
                        val a1 = if (growth == 0.0) x / periods.size else x * growth / (Math.pow(growth + 1, periods.size.toDouble()) - 1)
                        periods.mapIndexed { index, timeSpan -> timeSpan to a1 * Math.pow(1 + growth, index.toDouble()) }.toMap()
                    }
                    InterpolationType.ABSOLUTE -> {
                        val a1 = (x * 2.0 / periods.size - (periods.size - 1) * growth) / 2.0
                        periods.mapIndexed { index, timeSpan -> timeSpan to a1 + growth * index.toDouble() }.toMap()
                    }
                }
            }
        }
    }
}

enum class InterpolationBase {
    START,
    END
}

enum class InterpolationType {
    PROPORTIONAL,
    ABSOLUTE
}