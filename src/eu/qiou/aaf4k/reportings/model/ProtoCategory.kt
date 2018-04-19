package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.mergeReduce
import eu.qiou.aaf4k.util.time.TimeParameters

/**
 * the horizontally drilldownable properties, e.g. the year can be broken down to the single months, the
 *
 * entries is always attached to the atomic level of the drill-downs
 *
 */
class ProtoCategory(val name: String, val id: Int, val timeParameters: TimeParameters) : Drilldownable
{
    override fun getParents(): Collection<Drilldownable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun add(child: Drilldownable): Drilldownable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(child: Drilldownable): Drilldownable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val entries: MutableSet<ProtoEntry> = mutableSetOf()
    var drilldown: Drilldownable? = null

    override fun getChildren(): Collection<ProtoCategory>? {
        if(drilldown == null)
            return null

        return null
    }

    fun toDataMap(): Map<Int, Double> {
        return entries.map { it.toDataMap() }.reduce({ acc, map ->
            acc.mergeReduce(map, { a, b -> a + b })
        })
    }


}