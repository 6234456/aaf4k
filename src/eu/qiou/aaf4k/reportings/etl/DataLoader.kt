package eu.qiou.aaf4k.reportings.etl

/**
 *  account id -> account value
 */
interface DataLoader {
    fun loadData():Map<Int, Double>

    fun getDecimalPrecision():Int {
        return 2
    }
}