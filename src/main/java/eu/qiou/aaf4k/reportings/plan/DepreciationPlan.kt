package eu.qiou.aaf4k.reportings.plan

import eu.qiou.aaf4k.util.mergeReduce
import eu.qiou.aaf4k.util.roundUpTo
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.time.startOfNextMonth
import eu.qiou.aaf4k.util.time.times
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DepreciationPlan(
        numberOfPeriods: Int = 10,
        method: DepreciationMethod = DepreciationMethod.LINEAR,
        residualPercentage: Double? = null,
        residualAbsoluteValue: Double? = null,
        private val decimalPosition: Int = 2,
        private val depreciationStart: DepreciationStart = DepreciationStart.NEXT_MONTH,
        val customRate: List<Pair<Int, Double>>? = null
) {

    companion object {
        // Gewerbe
        val EstG_7_5_1_1 = DepreciationPlan(customRate = listOf(4 to 0.1, 3 to 0.05, 18 to 0.025))
        // Wohnimmobilien und sonstige
        val EstG_7_5_1_3 = DepreciationPlan(customRate = listOf(8 to 0.05, 6 to 0.025, 36 to 0.0125))
    }

    private val method = if (customRate != null) DepreciationMethod.CUSTOMIZED_ANNUAL_RATE else method
    private val numberOfPeriods = customRate?.fold(0) { acc, pair -> acc + pair.first } ?: numberOfPeriods
    private val residualPercentage = if (customRate != null) 1 - customRate.fold(0.0) { acc, pair -> acc + pair.second * pair.first }
    else residualPercentage

    private val residualAbsoluteValue = if (customRate != null) null else residualAbsoluteValue

    fun generate(assetValue:Double, start: LocalDate): Map<TimeSpan, Double> {
        val start1 = when(depreciationStart){
            DepreciationStart.NEXT_MONTH -> start.startOfNextMonth()
            DepreciationStart.THE_SAME_MONTH -> start.startOfNextMonth().minusMonths(1)
        }

        val residual = residualAbsoluteValue?: (assetValue * residualPercentage!!)

        if (residual < 0) {
            throw Exception("the residual value can not be negative.")
        }

        return when(method){
            DepreciationMethod.NUMBER_OF_TOTAL_YEARS, DepreciationMethod.CUSTOMIZED_ANNUAL_RATE -> {
                var cnt = -1

                val rates = when (method) {
                    DepreciationMethod.NUMBER_OF_TOTAL_YEARS -> {
                        val t = 1.until(numberOfPeriods + 1).reversed()
                        val total = t.reduce { acc, i -> acc + i }
                        t.map { e -> e * 1.0 / total }
                    }
                    DepreciationMethod.CUSTOMIZED_ANNUAL_RATE -> customRate!!.fold(listOf()) { acc, pair ->
                        acc + 1.until(pair.first + 1).map { pair.second }
                    }
                    else -> throw Exception("Cannot be reached. ERROR!")
                }

                rates.map { e -> (e * (assetValue - residual)).roundUpTo(decimalPosition) }.let {
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
    NUMBER_OF_TOTAL_YEARS,
    CUSTOMIZED_ANNUAL_RATE
}

enum class DepreciationStart {
    THE_SAME_MONTH,
    NEXT_MONTH
}