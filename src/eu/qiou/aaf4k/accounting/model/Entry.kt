package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.model.ProtoEntry

class Entry(id: Int, desc: String = "", category: Category) : ProtoEntry<Account>(id, desc, category) {

    override fun add(id: Int, value: Double): Entry {
        this.category.reporting.findAccountByID(id)?.let {
            return add(Account.from(it.toBuilder().setValue(v = value, decimalPrecision = it.decimalPrecision).build(), it.reportingType))
        }

        return this
    }

    override fun add(account: Account): Entry {
        if (!account.isAggregate)
            accounts.add(account)

        return this
    }

    fun balanced(): Boolean = residual() == 0.0

    private fun residual(): Double {
        return accounts.fold(0.0) { acc, e ->
            acc + if (e.isStatistical) 0.0 else e.displayValue
        }
    }

    private fun balanceWith(account: Account): Entry {
        return this.add(
                Account.from(
                        account.toBuilder().setValue(residual() * -1, account.decimalPrecision).build(),
                        account.reportingType
                )
        )
    }

    fun balanceWith(account: Int): Entry {
        return balanceWith(
                this.category.reporting.findAccountByID(account)!!)
    }
}