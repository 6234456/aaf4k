package eu.qiou.aaf4k.reportings.model


/**
 *  all the attribute at atomic level
 */
class ProtoEntry(val id: Int, val desc: String = "", val category: ProtoCategory) {

    init {
        category.entries.add(this)
    }

    var isActive:Boolean = true

    // in an entry there might be multiple accounts with the same id
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

    fun add(account: ProtoAccount): ProtoEntry {
        if (!account.isAggregate)
            accounts.add(account)

        return this
    }

    fun remove(account: ProtoAccount?): ProtoEntry {
        accounts.remove(account)

        return this
    }

    fun remove(id: Int): ProtoEntry {
        return remove(findById(id))
    }

    fun findById(id: Int): ProtoAccount? {
        return accounts.find { it.id == id }
    }

}