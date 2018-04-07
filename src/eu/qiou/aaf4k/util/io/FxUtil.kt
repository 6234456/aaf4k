package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.util.unit.ForeignExchange

object FxUtil {

    private val cache: MutableMap<ForeignExchange, Double> = mutableMapOf()

    /**
     *  fetch the exchange rate from external source, Internet / Database
     */
    fun fetch(target: ForeignExchange, source: FxFetcher = OandaFxFetcher(), useCache:Boolean = true):Double {

        if(useCache){
            if (cache.containsKey(target)){
                return displayRateAndReturn(target, cache.getValue(target))
            }
        }
        val rate = source.fetchFx(target, useCache)

        return displayRateAndReturn(target, rate)
    }

    private fun displayRateAndReturn(foreignExchange: ForeignExchange, value:Double):Double {
        cache.put(foreignExchange, value)

        foreignExchange.displayRate = value
        return value
    }
}