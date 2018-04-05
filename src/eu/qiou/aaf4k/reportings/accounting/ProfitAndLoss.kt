package eu.qiou.aaf4k.reportings.accounting

import eu.qiou.aaf4k.reportings.ProtoAccount
import eu.qiou.aaf4k.reportings.ProtoReporting
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.unit.CurrencyUnit

class ProfitAndLoss (id: Int, name: String, timeSpan: TimeSpan, displayUnit: CurrencyUnit = CurrencyUnit()): ProtoReporting(id=id, name=name, displayUnit = displayUnit, timeParameters = TimeParameters(timeSpan = timeSpan)){
    var result:Double = 0.0
    get() {
       return if(accounts.count() == 0) 0.0 else (accounts as Set<ProtoAccount>).fold(0.0){a, b -> a + b.displayValue}
    }
}