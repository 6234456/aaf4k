package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.reportings.model.ProtoEntity
import eu.qiou.aaf4k.reportings.model.ProtoReporting
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit

open class Reporting(id: Int, name: String, desc: String = "", structure: List<Account>,
                     displayUnit: ProtoUnit = CurrencyUnit(), entity: ProtoEntity = GlobalConfiguration.DEFAULT_ENTITY,
                     timeParameters: TimeParameters = GlobalConfiguration.DEFAULT_TIME_PARAMETERS) : ProtoReporting(id, name, desc, structure, displayUnit, entity, timeParameters)