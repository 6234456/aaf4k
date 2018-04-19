package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.mergeReduce

/**
 * the horizontally drilldownable properties, e.g. the year can be broken down to the single months, the
 *
 * entries is always attached to the atomic level of the drill-downs
 *
 */
class ProtoCategory(val name: String, val id: Int, val desc: String, val protoReporting: ProtoReporting)
{
    val entries: MutableSet<ProtoEntry> = mutableSetOf()

    fun toDataMap(): Map<Int, Double> {
        return entries.map { it.toDataMap() }.reduce({ acc, map ->
            acc.mergeReduce(map, { a, b -> a + b })
        })
    }
}