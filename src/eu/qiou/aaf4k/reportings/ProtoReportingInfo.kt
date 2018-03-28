package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.util.TimeSpan
import eu.qiou.aaf4k.util.CurrencyUnit
import eu.qiou.aaf4k.util.ProtoUnit
import eu.qiou.aaf4k.util.TimeAttribute
import java.time.LocalDate


/**
 * Information shared by Account and Reporting
 */
class ProtoReportingInfo(var displayUnit: ProtoUnit = CurrencyUnit(),
                         var timeAttribute: TimeAttribute = TimeAttribute.TIME_SPAN, var timeSpan: TimeSpan? = null, var timePoint: LocalDate? = null) {
}