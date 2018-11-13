package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mkString
import eu.qiou.aaf4k.util.strings.CollectionToString
import java.time.LocalDate


/**
 *  all the attribute at atomic level
 */
open class ProtoEntry<T : ProtoAccount>(val id: Int, val desc: String = "", val category: ProtoCategory<T>, val date: LocalDate = category.timeParameters.end) : JSONable {

    init {
        if (!category.timeParameters.contains(date))
            throw Exception("IllegalAttribute: date $date should be within the TimeSpan of the Category!")

        category.add(this)
    }

    var isActive:Boolean = true
    var isVisible: Boolean = true
    var isWritable: Boolean = true

    // in an entry there might be multiple accounts with the same id
    val accounts: MutableList<T> = mutableListOf()
    var isEmpty = true
        get() = accounts.count() == 0

    fun toDataMap(): Map<Int, Double> {
        if (!isActive || isEmpty)
            return mapOf()

        return accounts.groupBy { it.id }.mapValues {
            it.value.fold(0.0) { acc, e ->
                acc + e.decimalValue
            }
        }
    }

    fun unregister() {
        this.category.entries.remove(this)
    }

    open fun add(id: Int, value: Double): ProtoEntry<T> {
        this.category.reporting.findAccountByID(id)?.let {
            return add(it.toBuilder().setValue(v = value, decimalPrecision = it.decimalPrecision).build() as T)
        }

        return this
    }

    open fun add(account: T): ProtoEntry<T> {
        if (!account.isAggregate)
            accounts.add(account)

        return this
    }

    fun remove(account: T?): ProtoEntry<T> {
        accounts.remove(account)

        return this
    }

    fun remove(account: List<T>): ProtoEntry<T> {
        accounts.removeAll { it in account }
        return this
    }

    fun remove(id: Int): ProtoEntry<T> {
        return remove(findById(id))
    }

    fun clear() {
        accounts.clear()
    }

    fun findById(id: Int): List<T> {
        return accounts.filter { it.id == id }
    }

    fun existsId(id: Int): Boolean {
        return findById(id).count() > 0
    }

    operator fun plusAssign(account: T) {
        add(account)
    }

    operator fun minusAssign(account: T) {
        remove(account)
    }

    override fun toString(): String {
        return accounts.mkString(separator = ",\n", prefix = "<", affix = ">")
    }

    override fun toJSON(): String {
        return """{ "id": $id, "desc": "$desc", "accounts": ${CollectionToString.mkJSON(accounts)}, "date": "$date" }"""
    }

}