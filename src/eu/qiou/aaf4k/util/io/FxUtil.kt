package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.util.unit.ForeignExchange

object FxUtil {

    private val cache: MutableMap<ForeignExchange, Double> = mutableMapOf()

    /**
     *  fetch the exchange rate from external source, Internet / Database / File
     */
    fun fetch(target: ForeignExchange, source: FxFetcher = OandaFxFetcher(), useCache:Boolean = true):Double {

        if(useCache){
            if (cache.containsKey(target)){
                return setDisplayRateAndReturn(target, cache.getValue(target))
            }
        }

        val rate = source.fetchFx(target, useCache)
        return setDisplayRateAndReturn(target, rate)
    }

    private fun setDisplayRateAndReturn(foreignExchange: ForeignExchange, value:Double):Double {
        cache.put(foreignExchange, value)

        foreignExchange.displayRate = value
        return value
    }
}