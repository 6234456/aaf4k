package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mkString
import eu.qiou.aaf4k.util.strings.CollectionToString


/**
 *  all the attribute at atomic level
 */
class ProtoEntry(val id: Int, val desc: String = "", val category: ProtoCategory) : JSONable {

    init {
        category.entries.add(this)
    }

    var isActive:Boolean = true

    // in an entry there might be multiple accounts with the same id
    val accounts: MutableList<ProtoAccount> = mutableListOf()
    var isEmpty = true
        get() = accounts.count() == 0

    fun toDataMap(): Map<Int, Double> {
        if (!isActive || isEmpty)
            return mapOf()

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

    fun remove(account: List<ProtoAccount>): ProtoEntry {
        accounts.removeAll { it in account }
        return this
    }

    fun remove(id: Int): ProtoEntry {
        return remove(findById(id))
    }

    fun findById(id: Int): List<ProtoAccount> {
        return accounts.filter { it.id == id }
    }

    fun existsId(id: Int): Boolean {
        return findById(id).count() > 0
    }

    operator fun plusAssign(account: ProtoAccount) {
        add(account)
    }

    operator fun minusAssign(account: ProtoAccount) {
        remove(account)
    }

    override fun toString(): String {
        return accounts.mkString(separator = ",\n", prefix = "<", affix = ">")
    }

    override fun toJSON(): String {
        return CollectionToString.mkJSON(accounts)
    }

}