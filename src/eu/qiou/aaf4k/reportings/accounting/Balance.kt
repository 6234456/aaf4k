package eu.qiou.aaf4k.reportings.accounting

import eu.qiou.aaf4k.reportings.ProtoReporting
import eu.qiou.aaf4k.reportings.ProtoReportingInfo
import eu.qiou.aaf4k.util.time.TimeAttribute
import java.time.LocalDate

/**
 * Account of Dr. / Cr. differentiated with sign
 * the structure of the reporting defined external
 * Reportings with the same id belong to the same collection
 */
class Balance(id: Int, name: String, timePoint: LocalDate):ProtoReporting(id=id, name=name, reportingInfo = ProtoReportingInfo(timeAttribute = TimeAttribute.TIME_POINT, timePoint = timePoint)) {
    fun isBalanced(profitAndLoss: ProfitAndLoss):Boolean{
        return this.getAccountByID(Accounting.RESULT_ACCOUNT_ID)?.displayValue == profitAndLoss.result
    }
}