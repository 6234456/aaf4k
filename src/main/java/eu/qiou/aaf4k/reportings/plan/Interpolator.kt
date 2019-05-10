package eu.qiou.aaf4k.reportings.plan

import eu.qiou.aaf4k.util.roundUpTo
import eu.qiou.aaf4k.util.time.TimeSpan
import java.time.temporal.ChronoUnit

/**
 * @param interpolationBase START : set the base to Dec. END:  with target for the whole year divided through periods
 * @param interpolationType  whether the growth is proportional or absolute
 * @param growth    the growth
 * @param period    the plan period
 * @param interval  the intervals in the period
 * @param decimalPosition the positions after the decimal point, 2 by default. The difference is rounded up in the last period
 */

class Interpolator(val interpolationBase: InterpolationBase = InterpolationBase.START, val interpolationType: InterpolationType = InterpolationType.PROPORTIONAL, val growth: Double, val period: TimeSpan, val interval: ChronoUnit = period.drillDownTo.unit, val numberOfInterval: Long = period.drillDownTo.amount, val decimalPosition: Int = 2) {
    private val periods = period.drillDown(numberOfInterval, interval)

    fun parse(): (Double) -> Map<TimeSpan, Double> {
        return when (interpolationBase) {
            InterpolationBase.START -> { x1 ->
                val x = x1.roundUpTo(decimalPosition)
                periods.mapIndexed { index, timeSpan ->
                    timeSpan to
                            when (interpolationType) {
                                InterpolationType.PROPORTIONAL -> x * Math.pow(1 + growth, index.toDouble() + 1)
                                InterpolationType.ABSOLUTE -> x + growth * (index.toDouble() + 1)
                            }.roundUpTo(decimalPosition)
                }.toMap()
            }
            InterpolationBase.END -> { x1 ->
                val x = x1.roundUpTo(decimalPosition)
                when (interpolationType) {
                    InterpolationType.PROPORTIONAL -> {
                        val a1 = if (growth == 0.0) x / periods.size else x * growth / (Math.pow(growth + 1, periods.size.toDouble()) - 1)
                        periods.mapIndexed { index, timeSpan -> timeSpan to (a1 * Math.pow(1 + growth, index.toDouble())).roundUpTo(decimalPosition) }
                    }
                    InterpolationType.ABSOLUTE -> {
                        val a1 = (x * 2.0 / periods.size - (periods.size - 1) * growth) / 2.0
                        periods.mapIndexed { index, timeSpan -> timeSpan to (a1 + growth * index.toDouble()).roundUpTo(decimalPosition) }
                    }
                }.toMutableList().apply {
                    // round the decimal roundup error into the last period
                    val last = last()
                    this[size - 1] = last.first to (last.second + this.fold(x) { acc, pair -> acc - pair.second }).roundUpTo(decimalPosition)
                }.toMap()
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