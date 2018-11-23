package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mergeReduce
import eu.qiou.aaf4k.util.strings.CollectionToString


open class ProtoCategory<T : ProtoAccount>(val name: String, val id: Int, val desc: String, val reporting: ProtoReporting<T>) : JSONable {
    val entries: MutableList<ProtoEntry<T>> = mutableListOf()
    val timeParameters = reporting.timeParameters

    var isWritable: Boolean = true

    //might exist multi-thread problem
    var nextEntryIndex = 1

    init {
        reporting.add(this)
    }

    fun flatten(excludingInactive: Boolean = false): List<T> {
        return entries.fold(listOf<T>()) { acc, protoEntry ->
            if (excludingInactive && !protoEntry.isActive) acc else acc + protoEntry.accounts
        }
    }

    open fun deepCopy(reporting: ProtoReporting<T>): ProtoCategory<T> {
        return ProtoCategory(name, id, desc, reporting).apply {
            this@ProtoCategory.entries.forEach {
                it.deepCopy(this)
            }
            this.isWritable = this@ProtoCategory.isWritable
            this.nextEntryIndex = this@ProtoCategory.nextEntryIndex
        }
    }

    // no need to evoke add if in the constructor specified
    fun add(entry: ProtoEntry<T>) {
        if (entries.any { it.id == entry.id })
            throw Exception("Duplicated Entry-ID ${entry.id} in Category:'$name'")

        entries.add(entry)

        nextEntryIndex = Math.max(entry.id, nextEntryIndex) + 1
    }

    fun toDataMap(): Map<Long, Double> {
        return entries
                .map { it.toDataMap() }
                .fold(mapOf()) { acc, map ->
                    acc.mergeReduce(map) { a, b -> a + b }
                }
    }

    // entry-Id to entry
    fun toUncompressedDataMap(): Map<Int, Map<Long, Double>> {
        return entries.map { it.id to it.toDataMap() }.toMap()
    }

    fun searchForEntriesWith(id: Long): List<ProtoEntry<T>> {
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
        return """{ "id": $id, "desc": "$desc", "nextEntryIndex": $nextEntryIndex, "isWritable": $isWritable, "name": "$name", "entries": ${CollectionToString.mkJSON(entries)} }"""
    }

    override fun toString(): String {
        return CollectionToString.mkString(entries, prefix = "$id $name\n{@\n", affix = "\n@}", separator = "\n\n")
    }

}