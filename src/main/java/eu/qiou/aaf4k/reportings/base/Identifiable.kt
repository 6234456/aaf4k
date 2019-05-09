package eu.qiou.aaf4k.reportings.base

interface Identifiable : Comparable<Identifiable> {
    val id: Long

    override fun compareTo(other: Identifiable): Int {
        return id.compareTo(other.id)
    }
}