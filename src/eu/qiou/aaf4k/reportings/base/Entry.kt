package eu.qiou.aaf4k.reportings.base

import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mkString
import eu.qiou.aaf4k.util.strings.CollectionToString
import java.time.LocalDate

class Entry(val desc: String = "", val category: Category,
            val date: LocalDate = category.reporting.timeParameters.end,
            var isActive: Boolean = true,
            var isVisible: Boolean = true,
            var isWritable: Boolean = true) : JSONable {

    companion object {
        private const val UNINITIALIZED_ID = -1
    }

    // can only be set once or be reset
    var id: Int = UNINITIALIZED_ID
        set(value) {
            if (field == UNINITIALIZED_ID || value == UNINITIALIZED_ID || value == Category.RESULT_TRANSFER_ENTRY_ID)
                field = value
        }

    // in an entry there might be multiple accounts with the same id
    val accounts: MutableList<Account> = mutableListOf()

    init {
        if (!category.reporting.timeParameters.contains(date))
            throw Exception("IllegalAttribute: date $date should be within the TimeSpan of the Category!")

        category.add(this)
    }


    val isBalanced: Boolean
        get() = residual() == 0.0

    fun toDataMap(): Map<Long, Double> {
        if (!isActive || accounts.isEmpty()) return mapOf()

        return accounts.groupBy { it.id }.mapValues {
            it.value.fold(0.0) { acc, e ->
                acc + e.decimalValue
            }
        }
    }

    fun deepCopy(category: Category, keepID: Boolean = false): Entry {
        return Entry(desc = desc, category = category, date = date, isActive = isActive,
                isVisible = isVisible, isWritable = isWritable).apply {
            if (keepID) id = this@Entry.id
            this@Entry.accounts.forEach {
                this.add(it.deepCopy() as Account)
            }
        }
    }

    fun unregister() {
        this.category.entries.remove(this)
    }

    private fun residual(): Double {
        return accounts.fold(0.0) { acc, e ->
            acc + if (e.isStatistical) 0.0 else e.decimalValue
        }
    }

    private fun balanceWith(account: Account, ignoreZero: Boolean = true): Entry {
        val r = residual()
        if (ignoreZero && r == 0.0) return this
        return this.add(account.copyWith(r * -1, account.decimalPrecision) as Account)
    }

    fun balanceWith(account: Long, ignoreZero: Boolean = true): Entry {
        return balanceWith(
                this.category.reporting.findAccountByID(account)!! as Account, ignoreZero)
    }

    fun add(id: Long, value: Double): Entry {
        this.category.reporting.findAccountByID(id)?.let {
            return add(it.copyWith(value = value, decimalPrecision = it.decimalPrecision) as Account)
        }

        return this
    }

    fun add(account: Account): Entry {
        accounts.add(account)

        return this
    }

    fun remove(account: Account?): Entry {
        accounts.remove(account)

        return this
    }

    fun remove(account: List<ProtoAccount>): Entry {
        accounts.removeAll { it in account }
        return this
    }

    fun remove(id: Long): Entry {
        return remove(findById(id))
    }

    fun clear() {
        accounts.clear()
    }

    fun findById(id: Long): List<ProtoAccount> {
        return accounts.filter { it.id == id }
    }

    fun existsId(id: Long): Boolean {
        return findById(id).count() > 0
    }

    operator fun plusAssign(account: Account) {
        add(account)
    }

    operator fun minusAssign(account: Account) {
        remove(account)
    }

    override fun toString(): String {
        return accounts.mkString(separator = ",\n", prefix = "<", affix = ">")
    }

    override fun toJSON(): String {
        return """{ "id": $id, "desc": "$desc", "accounts": ${CollectionToString.mkJSON(accounts)}, "date": "$date", "isActive": $isActive, "isVisible": $isVisible, "isWritable": $isWritable}"""
    }


}