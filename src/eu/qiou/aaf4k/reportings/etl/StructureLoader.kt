package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.reportings.ProtoAccount
import eu.qiou.aaf4k.reportings.ProtoReporting

interface StructureLoader {
    fun load(reporting: ProtoReporting): List<ProtoAccount>
}