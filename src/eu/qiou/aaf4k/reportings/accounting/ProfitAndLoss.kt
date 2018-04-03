package eu.qiou.aaf4k.reportings.accounting

import eu.qiou.aaf4k.reportings.ProtoAccount
import eu.qiou.aaf4k.reportings.ProtoReporting
import eu.qiou.aaf4k.reportings.ProtoReportingInfo
import eu.qiou.aaf4k.util.time.TimeAttribute
import eu.qiou.aaf4k.util.time.TimeSpan

class ProfitAndLoss (id: Int, name: String, timeSpan: TimeSpan): ProtoReporting(id=id, name=name, reportingInfo = ProtoReportingInfo(timeAttribute = TimeAttribute.TIME_SPAN, timeSpan = timeSpan)){
    var result:Double = 0.0
    get() {
       return if(accounts.count() == 0) 0.0 else (accounts as Set<ProtoAccount>).fold(0.0){a, b -> a + b.displayValue}
    }
}