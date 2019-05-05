package eu.qiou.aaf4k.reportings.base

interface ReportingTranslator {
    fun translate(src: Map<Long, Double>): Map<Long, Double>

    fun translate(src: Reporting, target: Reporting): Reporting {
        return (src.deepCopy() as Reporting ).apply{ update(translate(src.toDataMap()))}
    }
}