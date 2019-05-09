package eu.qiou.aaf4k.reportings.etl

/**
 *  account id -> account value
 */
interface DataLoader {
    fun load(): MutableMap<Long, Double>
}