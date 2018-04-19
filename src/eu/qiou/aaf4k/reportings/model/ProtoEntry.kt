package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.time.TimeParameters


/**
 *  all the attribute at atomic level
 */
class ProtoEntry(val id: Int, val desc: String = "", val timeParameters: TimeParameters? = null, val entity: ProtoEntity? = null) {
    var isActive:Boolean = true
    var isVisible:Boolean = true

    val accounts: MutableList<ProtoAccount> = mutableListOf()
    val parameterList : MutableList<Drilldownable> = mutableListOf()

    operator fun contains(protoEntry: ProtoEntry): Boolean {
        if(this.timeParameters == null || protoEntry.timeParameters == null)
            return false

        return timeParameters.contains(protoEntry.timeParameters)
    }

    fun toDataMap(): Map<Int, Double> {
        return accounts.groupBy({ it.id }).mapValues {
            it.value.fold(0.0) { acc, e ->
                acc + e.decimalValue
            }
        }
    }

    fun timeParameterContains(protoEntry: ProtoEntry): Boolean {
        if(this.timeParameters == null || protoEntry.timeParameters == null)
            return false

        return timeParameters.contains(protoEntry.timeParameters)
    }

    fun entityContains(protoEntry: ProtoEntry): Boolean {
        if(this.entity == null || protoEntry.entity == null)
            return false

        return entity.contains(protoEntry.entity)
    }

    fun parameterList(protoEntry: ProtoEntry): Boolean {
        if(this.parameterList.count() != protoEntry.parameterList.count())
            return false

        return true
        /*
        this.parameterList.foldIndexed(true){
            i, acc, e ->
                acc && (protoEntry.parameterList[i] in e)
        }
        */
    }

}