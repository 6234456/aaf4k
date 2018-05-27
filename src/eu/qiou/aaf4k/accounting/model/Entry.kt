package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.reportings.model.ProtoEntry

class Entry(id: Int, desc: String = "", category: Category) : ProtoEntry(id, desc, category) {
    override val accounts: MutableList<ProtoAccount> = mutableListOf()

    fun balanced(): Boolean {
        return accounts.fold(0.0, { acc, e ->
            acc + e.displayValue
        }).equals(0.0)
    }
}