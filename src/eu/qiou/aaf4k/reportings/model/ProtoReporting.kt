package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.strings.CollectionToString
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit


/**
 * Class of ProtoReporting
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

    override fun toJSON():String{
        return CollectionToString.mkJSON(structure)
    }

    override fun toString(): String {
        return CollectionToString.mkString(structure)
    }
}