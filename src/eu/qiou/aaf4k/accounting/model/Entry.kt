package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.reportings.model.ProtoEntry

class Entry(id: Int, desc: String = "", category: Category) : ProtoEntry(id, desc, category) {
    override val accounts: MutableList<ProtoAccount> = mutableListOf()

    fun balanced(): Boolean = residual() == 0.0

    private fun residual(): Double {
        return accounts.fold(0.0) { acc, e ->
            acc + e.displayValue
        }
    }

    private fun balanceWith(account: Account): Entry {
        this.add(account.toBuilder().setValue(residual() * -1, account.decimalPrecision).build())
        return this
    }

    fun balanceWith(account: Int): Entry {
        balanceWith(this.category.reporting.findAccountByID(account) as Account)
        return this
    }
}