package eu.qiou.aaf4k.reportings.base

import eu.qiou.aaf4k.util.i18n.Message
import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mergeReduce
import eu.qiou.aaf4k.util.strings.CollectionToString

class Category(val name: String, val desc: String, val reporting: Reporting,
               val consolidationCategory: ConsolidationCategory? = null) : JSONable {
    companion object {
        /*
        Entry-ID reserved for the functional Entries
         */
        //transfer the P&L and OCI result to B/S
        private const val RESULT_TRANSFER_ENTRY_ID = 0
        private const val UNINITIALIZED_ID = -1
    }

    init {
        reporting.add(this)
    }

    val entries: MutableList<Entry> = mutableListOf()
    val timeParameters = reporting.timeParameters
    var isWritable: Boolean = true

    var id: Int = UNINITIALIZED_ID
        set(value) {
            if (field == UNINITIALIZED_ID || value == UNINITIALIZED_ID)
                field = value
        }

    // balance via result and oci to the balance stmt
    private val transferEntry = Entry(Message.of("transferResult"), this).apply {
        id = RESULT_TRANSFER_ENTRY_ID
    }

    private var nextEntryIndex = RESULT_TRANSFER_ENTRY_ID + 1


    fun flatten(excludingInactive: Boolean = false): List<ProtoAccount> {
        return entries.fold(listOf()) { acc, protoEntry ->
            if (excludingInactive && !protoEntry.isActive) acc else acc + protoEntry.accounts
        }
    }

    fun add(entry: Entry) {
        entries.add(
                entry.apply {
                    this.id = nextEntryIndex++
                }
        )
    }

    fun toDataMap(): Map<Long, Double> {
        return entries
                .map { it.toDataMap() }
                .fold(mapOf()) { acc, map ->
                    acc.mergeReduce(map) { a, b -> a + b }
                }
    }

    fun deepCopy(reporting: Reporting, keepID: Boolean = false): Category {
        return Category(name, desc, reporting, consolidationCategory).apply {
            this@Category.entries.forEach {
                if (it.id > RESULT_TRANSFER_ENTRY_ID) it.deepCopy(this, true)
            }
            isWritable = this@Category.isWritable
            nextEntryIndex = this@Category.nextEntryIndex
            if (keepID) id = this@Category.id
        }
    }

    // entry-Id to entry
    fun toUncompressedDataMap(): Map<Int, Map<Long, Double>> {
        return entries.map { it.id to it.toDataMap() }.toMap()
    }

    private fun searchForEntriesWith(id: Long): List<Entry> {
        return entries.filter { it.existsId(id) }
    }

    fun remove(id: Int): Category {
        return remove(findById(id))
    }

    fun remove(entry: Entry?): Category {
        entries.remove(entry)
        return this
    }

    fun deactivate(id: Int): Category {
        findById(id)?.isActive = false
        return this
    }

    fun findById(id: Int): Entry? {
        return entries.find { it.id == id }
    }

    override fun toJSON(): String {
        return """{ "id": $id, "desc": "$desc", "nextEntryIndex": $nextEntryIndex, "isWritable": $isWritable, "name": "$name", "consolidationCategory": ${consolidationCategory?.token}, "entries": ${CollectionToString.mkJSON(entries)} }"""
    }


    fun summarizeResult() {

        transferEntry.clear()
        val re = entries.filter { it.isActive }.map {
            it.accounts.filter { x -> x.reportingType == ReportingType.EXPENSE_LOSS || x.reportingType == ReportingType.REVENUE_GAIN }
                    .fold(0.0) { acc, account -> acc + account.decimalValue }
        }.fold(0.0) { acc, d -> acc + d }

        val oci = entries.filter { it.isActive }.map {
            it.accounts.filter { x -> x.reportingType == ReportingType.PROFIT_LOSS_NEUTRAL }
                    .fold(0.0) { acc, account -> acc + account.decimalValue }
        }.fold(0.0) { acc, d -> acc + d }

        if (Math.abs(re) != 0.0) {
            this.reporting.periodResultInBalance?.let {
                transferEntry.add(it.copyWith(re, it.decimalPrecision) as Account)
            }
        }

        if (Math.abs(oci) != 0.0) {
            this.reporting.oci?.let {
                transferEntry.add(it.copyWith(oci, it.decimalPrecision) as Account)
            }
        }
    }
}


enum class ConsolidationCategory(val token: Int) {
    INIT_EQUITY(0),
    SUBSEQUENT_EQUITY(1),
    PAYABLES_RECEIVABLES(2),
    UNREALIZED_PROFIT_AND_LOSS(3),
    REVENUE_EXPENSE(4)
}