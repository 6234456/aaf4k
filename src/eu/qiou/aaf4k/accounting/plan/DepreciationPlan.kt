package eu.qiou.aaf4k.accounting.plan

import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.time.startOfNextMonth
import eu.qiou.aaf4k.util.time.times
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DepreciationPlan(val assetValue: Double, val residualPercentage: Double? = null, val residualAbsoluteValue: Double? = null,
                       val numberOfPeriods: Int, val method: DepreciationMethod = DepreciationMethod.LINEAR, val decimalPosition: Int = 2) {

    init {
        assert(residualAbsoluteValue == null || residualPercentage == null)
    }

    fun generate(start: LocalDate): Map<TimeSpan, Double> {
        val start1 = start.startOfNextMonth()
        return Interpolator(InterpolationBase.START, InterpolationType.ABSOLUTE,
                growth = ((residualAbsoluteValue
                        ?: (assetValue * residualPercentage!!)) - assetValue) / numberOfPeriods,
                period = TimeSpan(start1, start1 + (ChronoUnit.MONTHS * (numberOfPeriods - 1))),
                decimalPosition = decimalPosition
        ).parse()(assetValue)
    }

}

enum class DepreciationMethod {
    LINEAR,
    NUMBER_OF_TOTAL_YEARS
}