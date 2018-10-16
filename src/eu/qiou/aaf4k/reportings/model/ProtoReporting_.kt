package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.ProtoUnit

interface ProtoReporting_ {
    val id: Int
    val name: String
    val desc: String
    val structure: List<ProtoAccount_>
    val displayUnit: ProtoUnit
    val entity: ProtoEntity
    val timeParameters: TimeParameters


}