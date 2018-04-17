package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.time.TimeParameters

/**
 * the horizontally drilldownable properties, e.g. the year can be broken down to the single months, the
 *
 * entries is always attached to the atomic level of the drill-downs
 *
 */
class ProtoCategory<E : ProtoEntry<*>>(val name:String, val id:Int, val timeParameters: TimeParameters):Drilldownable
{
    override fun getParent(): Collection<Drilldownable>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun add(child: Drilldownable): Drilldownable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(child: Drilldownable): Drilldownable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val entries: MutableSet<E> = mutableSetOf()
    var drilldown: Drilldownable? = null

    override fun getChildren(): Collection<ProtoCategory<*>>? {
        if(drilldown == null)
            return null

        return null

    }

}