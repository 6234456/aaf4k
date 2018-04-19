package eu.qiou.aaf4k.reportings.model


/**
 *  all the attribute at atomic level
 */
class ProtoEntry(val id: Int, val desc: String = "", val category: ProtoCategory) {
    var isActive:Boolean = true
    var isVisible:Boolean = true

    val accounts: MutableList<ProtoAccount> = mutableListOf()

    fun toDataMap(): Map<Int, Double> {
        if(! isActive)
            return mutableMapOf()

        return accounts.filter { ! it.isStatistical }.groupBy({ it.id }).mapValues {
            it.value.fold(0.0) { acc, e ->
                acc + e.decimalValue
            }
        }
    }
}