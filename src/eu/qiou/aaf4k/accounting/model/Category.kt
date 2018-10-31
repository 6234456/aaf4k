package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.model.ProtoCategory

class Category(name: String, id: Int, desc: String, reporting: Reporting) : ProtoCategory<Account>(name, id, desc, reporting) {

    // balance via result and oci to the balance stmt
    val transferEntry = Entry(this.nextEntryIndex, "transfer result/OCI", this).apply {
        isVisible = false
    }

    fun summarizeResult() {
        transferEntry.clear()

        val re = entries.map {
            it.accounts.filter { it.reportingType == ReportingType.EXPENSE_LOSS || it.reportingType == ReportingType.REVENUE_GAIN }
                    .fold(0.0) { acc, account -> acc + account.decimalValue }
        }.fold(0.0) { acc, d -> acc + d }

        val oci = entries.map {
            it.accounts.filter { it.reportingType == ReportingType.PROFIT_LOSS_NEUTRAL }
                    .fold(0.0) { acc, account -> acc + account.decimalValue }
        }.fold(0.0) { acc, d -> acc + d }

        if (Math.abs(re) != 0.0) {
            (this.reporting as Reporting).retainedEarning?.let {
                transferEntry.add(Account.from(it.toBuilder().setValue(re, it.decimalPrecision).build(), it.reportingType))
            }
        }

        if (Math.abs(re) != 0.0) {
            (this.reporting as Reporting).oci?.let {
                transferEntry.add(Account.from(it.toBuilder().setValue(oci, it.decimalPrecision).build(), it.reportingType))
            }
        }
    }
}
