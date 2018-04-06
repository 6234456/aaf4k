package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.util.unit.ForeignExchange

object FxUtil {

    private val cache: MutableMap<ForeignExchange, Double> = mutableMapOf()

    /**
     *  fetch the exchange rate from external source, Internet / Database
     */
    fun fetch(target: ForeignExchange, forceRefresh:Boolean = false):Double {

        if(!forceRefresh){
            if (cache.containsKey(target)){
                return displayRateAndReturn(target, cache.getValue(target))
            }
        }

        val rate = 1.2345

        return displayRateAndReturn(target, rate, true)
    }

    private fun displayRateAndReturn(foreignExchange: ForeignExchange, value:Double, toCache:Boolean = false):Double {
        if(toCache)
            cache.put(foreignExchange, value)

        foreignExchange.displayRate = value
        return value
    }

    fun fetchFromURL(target: ForeignExchange):Double {

        return 0.0
    }
}