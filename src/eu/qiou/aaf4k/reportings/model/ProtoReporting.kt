package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mergeReduce
import eu.qiou.aaf4k.util.strings.CollectionToString
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit


/**
 * Class of ProtoReporting
 *
 * the object model of the reporting
 *
 * ProtoReporting - ProtoCategory - ProtoEntry - (ProtoAccount)
 *
 * ProtoAccount is not always attached to a ProtoEntry
 *
 * Reporting can be deemed as a list of <b>structured</b> ProtoAccounts
 * while Category is a set of Entries, which is not structured
 *
 * initial value of ProtoReporting is null, each state of the Reporting is immutable. The Reporting is updated by means of Category
 * ProtoReporting = ProtoReporting + ProtoCategory
 *
 * ProtoCategory is a collection of ProtoEntries, which represent the delta of ProtoReporting.
 *
 * @property structure list of accounts in structure
 */
open class ProtoReporting(val id: Int, val name: String, val desc: String = "", val structure: List<ProtoAccount>,
                          val displayUnit: ProtoUnit = CurrencyUnit(), val entity: ProtoEntity = GlobalConfiguration.DEFAULT_ENTITY,
                          val timeParameters: TimeParameters = GlobalConfiguration.DEFAULT_TIME_PARAMETERS) : JSONable {

    val categories: MutableSet<ProtoCategory> = mutableSetOf()

    fun mergeCategories(): Map<Int, Double> {
        return categories.map { it.toDataMap() }.reduce { acc, map ->
            acc.mergeReduce(map, { a, b -> a + b })
        }
    }

    /**
     * return one dimensional array of atomic accounts
     */
    fun flatten(): List<ProtoAccount> {
        return structure.filter { !it.isStatistical }.map { it.flatten() }.reduce { acc, mutableList ->
            acc.addAll(mutableList)
            acc
        } as List<ProtoAccount>
    }

    /**
     * after update through the categories
     * get the reporting
     */
    fun generate(): ProtoReporting {
        return update(mergeCategories())
    }

    fun update(entry: ProtoEntry): ProtoReporting {
        return ProtoReporting(id, name, desc,
                structure.map { it.deepCopy(entry) }
                , displayUnit, entity, timeParameters)
    }

    fun update(category: ProtoCategory): ProtoReporting {
        return ProtoReporting(id, name, desc,
                structure.map { it.deepCopy(category) }
                , displayUnit, entity, timeParameters)
    }

    fun update(data: Map<Int, Double>): ProtoReporting {
        return ProtoReporting(id, name, desc,
                structure.map { it.deepCopy(data) }
                , displayUnit, entity, timeParameters)
    }

    override fun toJSON():String{
        return CollectionToString.mkJSON(structure)
    }

    override fun toString(): String {
        return CollectionToString.mkString(structure)
    }
}