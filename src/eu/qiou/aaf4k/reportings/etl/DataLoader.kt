package eu.qiou.aaf4k.reportings.etl

/**
 *  account id -> account value
 */
interface DataLoader {
    fun load():Map<Int, Long>

    fun getDecimalPrecision():Int {
        return 2
    }
}