package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.reportings.accounting.Accounting
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit
import eu.qiou.aaf4k.util.time.TimeAttribute
import java.time.LocalDate
import java.util.*


/**
 * Information shared by Account and Reporting
 */
data class ProtoReportingInfo(var displayUnit: ProtoUnit = CurrencyUnit(), var currency: Currency? = if(displayUnit is CurrencyUnit) Accounting.DEFAULT_CURRENCY else null,
                              var timeAttribute: TimeAttribute = TimeAttribute.TIME_SPAN, var timeSpan: TimeSpan? = null, var timePoint: LocalDate? = null) {
}