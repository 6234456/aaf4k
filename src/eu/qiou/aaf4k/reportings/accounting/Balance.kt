package eu.qiou.aaf4k.reportings.accounting

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.reportings.ProtoReporting
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import java.time.LocalDate

/**
 * Account of Dr. / Cr. differentiated with sign
 * the structure of the reporting defined external
 * Reportings with the same id belong to the same collection
 */
class Balance(id: Int, name: String, timePoint: LocalDate, displayUnit: CurrencyUnit= CurrencyUnit()):ProtoReporting(id=id, name=name, displayUnit= displayUnit, timeParameters = TimeParameters(timePoint = timePoint)) {
    fun isBalanced(profitAndLoss: ProfitAndLoss):Boolean{
        return this.getAccountByID(GlobalConfiguration.RESULT_ACCOUNT_ID)?.displayValue == profitAndLoss.result
    }
}