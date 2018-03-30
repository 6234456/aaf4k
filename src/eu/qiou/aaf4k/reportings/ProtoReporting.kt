package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.reportings.etl.DataLoader
import eu.qiou.aaf4k.reportings.etl.StructureLoader


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

    fun loadStructure(structureLoader: StructureLoader):ProtoReporting {
        return structureLoader.loadStructure(this)
    }

    fun loadData(dataLoader: DataLoader):ProtoReporting{
        if(structure.count() == 0)
            throw Exception("Try to load data into the empty structure!")

        dataLoader.loadData().forEach({ a, b ->
            val acc = this.getAccountByID(a)
            if(!b.equals(0L)){
                if(acc == null)
                    throw Exception("the account id '" + a + "' missing!" )
                else {
                    acc.decimalPrecision = dataLoader.getDecimalPrecision()
                    acc.displayValue = b
                }
            }
        })

        return this
    }

    override fun toString(): String {
        return structure.fold(""){a, b -> a + b.toString() + "\n"}
    }
}