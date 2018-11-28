package eu.qiou.aaf4k.accounting.model

interface ReportingTranslator {
    fun translate(src: Map<Long, Double>): Map<Long, Double>

    fun translate(src: Reporting, target: Reporting): Reporting {
        return src.updateStructure { target.update(translate(src.toDataMap())).structure }
    }
}