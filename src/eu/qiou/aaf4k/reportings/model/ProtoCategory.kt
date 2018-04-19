package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.mergeReduce


class ProtoCategory(val name: String, val id: Int, val desc: String, val reporting: ProtoReporting)
{
    val entries: MutableSet<ProtoEntry> = mutableSetOf()
    val entity = reporting.entity
    val timeParameters = reporting.timeParameters

    init {
        reporting.categories.add(this)
    }

    fun toDataMap(): Map<Int, Double> {
        return entries.map { it.toDataMap() }.reduce({ acc, map ->
            acc.mergeReduce(map, { a, b -> a + b })
        })
    }

    fun remove(id: Int): ProtoCategory {
        return remove(findById(id))
    }

    fun remove(entry: ProtoEntry?): ProtoCategory {
        entries.remove(entry)
        return this
    }

    fun deactivate(id: Int): ProtoCategory {
        findById(id)?.isActive = false
        return this
    }

    fun findById(id: Int): ProtoEntry? {
        return entries.find { it.id == id }
    }
}