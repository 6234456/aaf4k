package eu.qiou.aaf4k.reportings


/**
 * @property accounts flattened set of accounts
 * @property structure list of accounts in structure
 */
open class ProtoReporting(val id:Int, val name: String, var desc: String="",var accounts: MutableSet<ProtoAccount> = mutableSetOf(), var structure: MutableList<AggregateAccount> = mutableListOf(),
                     var reportingInfo: ProtoReportingInfo = ProtoReportingInfo()) {
    fun getAccountByID(id: Int): ProtoAccount?{
        return accounts.find { it.id == id }
    }

    fun addAggreateAccount(aggregateAccount: AggregateAccount){
        structure.add(aggregateAccount)
        accounts.addAll(aggregateAccount.flatten())
    }

    fun existsDuplicateAccounts():Boolean {
        return this.accounts.count() != structure.fold(0){a, b -> a + b.count()}
    }

    override fun toString(): String {
        return structure.fold(""){a, b -> a + b.toString() + "\n"}
    }
}