package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.util.unit.ForeignExchange

object FxUtil {

    /**
     *  fetch the exchange rate from external source, Internet / Database
     */
    fun fetch(target: ForeignExchange):Double {
        val rate = 1.2345
        target.displayRate = rate
        return rate
    }
}