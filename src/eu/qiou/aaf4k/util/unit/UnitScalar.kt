package eu.qiou.aaf4k.util.unit

enum class UnitScalar(val scalar: Double, val token: String){
    UNIT(1.0, "unit"),
    THOUSAND(1000.0, "k"),
    TEN_THOUSAND(10000.0, "10k"),
    MILLION(1000000.0, "million"),
    HUNDRED_MILLION(10000000.0, "100million"),
    BILLION(1000000000.0, "billion");

    override fun toString(): String {
        return scalar.toString()
    }

}
