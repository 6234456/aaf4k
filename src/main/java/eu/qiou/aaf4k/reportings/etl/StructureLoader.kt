package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.reportings.base.ProtoAccount

interface StructureLoader {
    /**
     * load the structure to the reporting
     */
    fun load(): List<ProtoAccount>
}