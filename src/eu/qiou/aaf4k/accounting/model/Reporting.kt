package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.reportings.model.ProtoCategory
import eu.qiou.aaf4k.reportings.model.ProtoEntity
import eu.qiou.aaf4k.reportings.model.ProtoEntry
import eu.qiou.aaf4k.reportings.model.ProtoReporting
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit

open class Reporting(id: Int, name: String, desc: String = "", structure: List<Account>,
                     displayUnit: ProtoUnit = CurrencyUnit(), entity: ProtoEntity = GlobalConfiguration.DEFAULT_ENTITY,
                     timeParameters: TimeParameters = GlobalConfiguration.DEFAULT_TIME_PARAMETERS) : ProtoReporting<Account>(id, name, desc, structure, displayUnit, entity, timeParameters) {

    override fun update(data: Map<Int, Double>, updateMethod: (Double, Double) -> Double): Reporting {
        return Reporting(id, name, desc,
                structure.map { it.deepCopy<Account>(data, updateMethod) }
                , displayUnit, entity, timeParameters)
    }

    override fun update(entry: ProtoEntry<Account>, updateMethod: (Double, Double) -> Double): Reporting {
        return Reporting(id, name, desc,
                structure.map { it.deepCopy<Account>(entry, updateMethod) }
                , displayUnit, entity, timeParameters)
    }

    override fun update(category: ProtoCategory<Account>, updateMethod: (Double, Double) -> Double): Reporting {
        return Reporting(id, name, desc,
                structure.map { it.deepCopy<Account>(category, updateMethod) }
                , displayUnit, entity, timeParameters)
    }

}