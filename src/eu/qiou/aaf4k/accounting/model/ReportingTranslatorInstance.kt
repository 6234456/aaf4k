package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.etl.AccountingFrame

class ReportingTranslatorInstance(private val path: String) : ReportingTranslator {
    private val acc = accumulator()

    override fun translate(src: Map<Long, Double>): Map<Long, Double> {
        return acc(src)
    }

    private fun accumulator(): (Map<Long, Double>) -> Map<Long, Double> {
        return { x: Map<Long, Double> ->
            AccountingFrame.inflate(0, "", path).structure.map {
                it.id to it.subAccounts!!.fold(0.0)
                { y, e -> y + x.getOrDefault(e.id, 0.0) }
            }.toMap().filter { it.value != 0.0 }
        }
    }
}