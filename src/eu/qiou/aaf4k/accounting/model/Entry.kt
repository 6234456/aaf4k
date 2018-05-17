package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.model.ProtoCategory
import eu.qiou.aaf4k.reportings.model.ProtoEntry

class Entry(override val id: Int, override val desc: String = "", override val category: ProtoCategory) : ProtoEntry(id, desc, category) {
    override val accounts: MutableList<Account> = mutableListOf()
}