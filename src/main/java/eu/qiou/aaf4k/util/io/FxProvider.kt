package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.util.unit.ForeignExchange

abstract class FxProvider {
    private val cache: MutableMap<ForeignExchange, Double> = mutableMapOf()

    abstract fun fetchFxFromSource(target: ForeignExchange): Double

    open fun baseFx(target: ForeignExchange): Map<java.time.LocalDate, Double> {
        return HashMap()
    }

    fun fetch(target: ForeignExchange, useCache: Boolean = true): Double {
        if (target.functionalCurrency == target.reportingCurrency)
            return 1.0

        if (useCache) {
            if (cache.containsKey(target)) {
                return cache[target]!!
            }
        }

        val res = fetchFxFromSource(target)

        cache[target] = res
        return res
    }

    fun clearCache() {
        cache.clear()
    }

    fun toXls(target: ForeignExchange, file: String) {
        ExcelUtil.writeData(path = file, data = baseFx(target), header = listOf("Date", "Value"), sheetName = "${target.functionalCurrency.currencyCode}-${target.reportingCurrency.currencyCode}")
    }
}