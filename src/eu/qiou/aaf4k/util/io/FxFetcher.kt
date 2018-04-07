package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.util.unit.ForeignExchange

abstract class FxFetcher {
    private val cache: MutableMap<ForeignExchange, Double> = mutableMapOf()

    abstract fun fetchFxFromSource(target: ForeignExchange):Double

    fun fetchFx(target: ForeignExchange, useCache:Boolean = true):Double {
        if(useCache){
            if (cache.containsKey(target)){
                return cache.get(target)!!
            }
        }

        val res = fetchFxFromSource(target)

        cache.put(target, res)
        return res
    }

    fun clearCache(){
        cache.clear()
    }


}