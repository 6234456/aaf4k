package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.util.unit.ForeignExchange

abstract class FxProvider {
    private val cache: MutableMap<ForeignExchange, Double> = mutableMapOf()

    abstract fun fetchFxFromSource(target: ForeignExchange): Double

    fun fetch(target: ForeignExchange, useCache: Boolean = true): Double {
        if (target.functionalCurrency.equals(target.reportingCurrency))
            return 1.0

        if (useCache) {
            if (cache.containsKey(target)) {
                return cache.get(target)!!
            }
        }

        val res = fetchFxFromSource(target)

        cache.put(target, res)
        return res
    }

    fun clearCache() {
        cache.clear()
    }


}