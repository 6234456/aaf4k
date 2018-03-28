package eu.qiou.aaf4k.util

/**
 * TODO("token should be replaced with format functions")
 */
enum class UnitScale(val scale: Double, val token: String){
    UNIT(1.0, ""),
    THOUSAND(1000.0, "T"),
    TEN_THOUSAND(10000.0, ""),
    MILLION(1000000.0, "Mio."),
    TEN_MILLION(10000000.0, ""),
    BILLION(1000000000.0, "Mrd.")
}
