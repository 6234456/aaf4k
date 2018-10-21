package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mergeReduce
import eu.qiou.aaf4k.util.strings.CollectionToString


open class ProtoCategory<T : ProtoAccount>(val name: String, val id: Int, val desc: String, val reporting: ProtoReporting<T>) : JSONable {
    val entries: MutableList<ProtoEntry<T>> = mutableListOf()
    val timeParameters = reporting.timeParameters

    //might exist multi-thread problem
    var nextEntryIndex = 1

    init {
        reporting.addCategory(this)
    }

    fun addEntry(entry: ProtoEntry<T>) {
        if (entries.any { it.id == entry.id })
            throw Exception("Duplicated Entry-ID ${entry.id}")

        entries.add(entry)

        nextEntryIndex = Math.max(entry.id, nextEntryIndex) + 1
    }

    fun toDataMap(): Map<Int, Double> {
        return entries
                .map { it.toDataMap() }
                .fold(mapOf()) { acc, map ->
                    acc.mergeReduce(map) { a, b -> a + b }
                }
    }

    fun searchForEntriesWith(id: Int): List<ProtoEntry<T>> {
        return entries.filter { it.existsId(id) }
    }

    fun remove(id: Int): ProtoCategory<T> {
        return remove(findById(id))
    }

    fun remove(entry: ProtoEntry<T>?): ProtoCategory<T> {
        entries.remove(entry)
        return this
    }

    fun deactivate(id: Int): ProtoCategory<T> {
        findById(id)?.isActive = false
        return this
    }

    fun findById(id: Int): ProtoEntry<T>? {
        return entries.find { it.id == id }
    }

    override fun toJSON(): String {
        return CollectionToString.mkJSON(entries)
    }

    override fun toString(): String {
        return CollectionToString.mkString(entries, prefix = "$id $name\n{@\n", affix = "\n@}", separator = "\n\n")
    }

}