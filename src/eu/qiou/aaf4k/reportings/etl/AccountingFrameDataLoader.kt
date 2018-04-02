package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.reportings.ProtoAccount
import eu.qiou.aaf4k.reportings.ProtoReportingInfo

interface AccountingFrameDataLoader:DataLoader {
    fun loadAccountingFrameData():MutableMap<Int, Pair<String, Double>>

    fun parseAccount(entry: Map.Entry<Int, Pair<String, Double>>, reportingInfo: ProtoReportingInfo):ProtoAccount
}