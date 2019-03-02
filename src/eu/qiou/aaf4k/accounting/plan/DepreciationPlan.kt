package eu.qiou.aaf4k.accounting.plan

import eu.qiou.aaf4k.util.mergeReduce
import eu.qiou.aaf4k.util.roundUpTo
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.time.startOfNextMonth
import eu.qiou.aaf4k.util.time.times
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DepreciationPlan(
        val numberOfPeriods: Int, val method: DepreciationMethod = DepreciationMethod.LINEAR,
        val residualPercentage: Double? = null, val residualAbsoluteValue: Double? = null,
        val decimalPosition: Int = 2,
        val depreciationStart: DepreciationStart = DepreciationStart.NEXT_MONTH) {

    init {
        assert(residualAbsoluteValue == null || residualPercentage == null)
    }

    fun generate(assetValue:Double, start: LocalDate): Map<TimeSpan, Double> {
        val start1 = when(depreciationStart){
            DepreciationStart.NEXT_MONTH -> start.startOfNextMonth()
            DepreciationStart.THE_SAME_MONTH -> start.startOfNextMonth().minusMonths(1)
        }

        val residual = residualAbsoluteValue?: (assetValue * residualPercentage!!)

        return when(method){
            DepreciationMethod.NUMBER_OF_TOTAL_YEARS ->{
                var cnt = -1
                val t = 1.until(numberOfPeriods).reversed()
                val total = t.reduce { acc, i -> acc + i }
                t.map { e -> (e * (assetValue - residual) / total).roundUpTo(decimalPosition) }.let {
                    val delta = (assetValue - residual).roundUpTo(decimalPosition) - it.reduce { acc, d -> acc + d }
                    it.toMutableList().apply {
                        this[lastIndex] += delta
                    }.fold(mapOf<TimeSpan, Double>()){ acc, d ->
                        cnt++
                        acc.mergeReduce(
                            Interpolator(InterpolationBase.END, InterpolationType.ABSOLUTE,
                                    growth = 0.0,
                                    period = TimeSpan(start1, start1 + (ChronoUnit.MONTHS * (12 - 1))) + (ChronoUnit.MONTHS * (cnt * 12)),
                                    decimalPosition = decimalPosition
                            ).parse()(
                                    d
                            )
                        ){
                            a, b -> a + b
                        }
                    }
                }
            }
            else ->
                Interpolator(InterpolationBase.END, InterpolationType.ABSOLUTE,
                        growth = 0.0,
                        period = TimeSpan(start1, start1 + (ChronoUnit.MONTHS * (numberOfPeriods - 1))),
                        decimalPosition = decimalPosition
                ).parse()(
                        assetValue - residual
                )
        }
    }

}

enum class DepreciationMethod {
    LINEAR,
    NUMBER_OF_TOTAL_YEARS
}

enum class DepreciationStart {
    THE_SAME_MONTH,
    NEXT_MONTH
}