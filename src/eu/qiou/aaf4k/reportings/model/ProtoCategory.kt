package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mergeReduce
import eu.qiou.aaf4k.util.strings.CollectionToString


open class ProtoCategory(val name: String, val id: Int, val desc: String, val reporting: ProtoReporting) : JSONable
{
    val entries: MutableSet<out ProtoEntry> = mutableSetOf()
    val entity = reporting.entity
    val timeParameters = reporting.timeParameters

    init {
        reporting.categories.add(this)
    }

    fun toDataMap(): Map<Int, Double> {
        return entries
                .map { it.toDataMap() }
                .fold(mapOf()) { acc, map ->
                    acc.mergeReduce(map, { a, b -> a + b })
                }
    }

    fun searchForEntriesWith(id: Int): List<ProtoEntry> {
        return entries.filter { it.existsId(id) }
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

    override fun toJSON(): String {
        return CollectionToString.mkJSON(entries)
    }

    override fun toString(): String {
        return CollectionToString.mkString(entries, prefix = "{@", affix = "@}")
    }

}