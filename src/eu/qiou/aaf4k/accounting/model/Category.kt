package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.model.ProtoCategory
import eu.qiou.aaf4k.reportings.model.ProtoReporting
import java.util.*

class Category(name: String, id: Int, desc: String, reporting: Reporting, val consolidationCategory: ConsolidationCategory? = null) : ProtoCategory<Account>(name, id, desc, reporting) {

    private val msg = ResourceBundle.getBundle("aaf4k")
    // balance via result and oci to the balance stmt
    private val transferEntry = Entry(0, msg.getString("transferResult"), this).apply {
        isVisible = false
    }

    override fun deepCopy(reporting: ProtoReporting<Account>): ProtoCategory<Account> {
        return Category(name, id, desc, reporting as Reporting).apply {
            (this@Category.entries as Collection<Entry>).forEach {
                if (it.id != 0)
                    it.deepCopy(this)
            }
            this.isWritable = this@Category.isWritable
            this.nextEntryIndex = this@Category.nextEntryIndex
        }
    }

    fun summarizeResult() {
        transferEntry.clear()

        val re = entries.filter { it.isActive }.map {
            it.accounts.filter { it.reportingType == ReportingType.EXPENSE_LOSS || it.reportingType == ReportingType.REVENUE_GAIN }
                    .fold(0.0) { acc, account -> acc + account.decimalValue }
        }.fold(0.0) { acc, d -> acc + d }

        val oci = entries.filter { it.isActive }.map {
            it.accounts.filter { it.reportingType == ReportingType.PROFIT_LOSS_NEUTRAL }
                    .fold(0.0) { acc, account -> acc + account.decimalValue }
        }.fold(0.0) { acc, d -> acc + d }

        if (Math.abs(re) != 0.0) {
            (this.reporting as Reporting).periodResultInBalance?.let {
                transferEntry.add(Account.from(it.toBuilder().setValue(re, it.decimalPrecision).build(), it.reportingType))
            }
        }

        if (Math.abs(oci) != 0.0) {
            (this.reporting as Reporting).oci?.let {
                transferEntry.add(Account.from(it.toBuilder().setValue(oci, it.decimalPrecision).build(), it.reportingType))
            }
        }
    }
}
