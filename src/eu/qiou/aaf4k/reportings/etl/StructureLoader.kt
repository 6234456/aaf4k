package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.reportings.model.ProtoReporting

interface StructureLoader {
    /**
     * load the structure to the reporting
     * @param the reporting object to hold the structure
     */
    fun loadStructure(reporting: ProtoReporting): ProtoReporting
}