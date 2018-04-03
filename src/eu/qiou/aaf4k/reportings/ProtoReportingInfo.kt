package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.util.TimeSpan
import eu.qiou.aaf4k.util.CurrencyUnit
import eu.qiou.aaf4k.util.ProtoUnit
import eu.qiou.aaf4k.util.TimeAttribute
import java.time.LocalDate
import java.util.*


/**
 * Information shared by Account and Reporting
 */
data class ProtoReportingInfo(var displayUnit: ProtoUnit = CurrencyUnit(), var currency: Currency? = if(displayUnit is CurrencyUnit) Currency.getInstance("EUR") else null,
                              var timeAttribute: TimeAttribute = TimeAttribute.TIME_SPAN, var timeSpan: TimeSpan? = null, var timePoint: LocalDate? = null) {
}