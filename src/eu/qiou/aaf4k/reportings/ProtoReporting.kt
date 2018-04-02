package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.reportings.etl.AccountingFrameDataLoader
import eu.qiou.aaf4k.reportings.etl.DataLoader
import eu.qiou.aaf4k.reportings.etl.StructureLoader
import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.strings.CollectionToString


/**
 * @property accounts flattened set of accounts
 * @property structure list of accounts in structure
 */
open class ProtoReporting(val id:Int, val name: String, var desc: String="",var accounts: MutableSet<ProtoAccount> = mutableSetOf(), var structure: MutableList<AggregateAccount> = mutableListOf(),
                     var reportingInfo: ProtoReportingInfo = ProtoReportingInfo()) : JSONable {
    fun getAccountByID(id: Int): ProtoAccount?{
        return accounts.find { it.id == id }
    }

    fun getComponentAccountByID(id: Int):ProtoAccount?{
        return structure.find { it.id == id }
    }

    fun addAggreateAccount(aggregateAccount: AggregateAccount){
        structure.add(aggregateAccount)
        accounts.addAll(aggregateAccount.flatten())
    }

    fun existsDuplicateAccounts():Boolean {
        return this.accounts.count() != structure.fold(0){a, b -> a + b.count()}
    }

    open fun loadStructure(structureLoader: StructureLoader):ProtoReporting {
        return structureLoader.loadStructure(this)
    }

    open fun loadData(dataLoader: DataLoader):ProtoReporting{
        if(structure.count() == 0)
            throw Exception("Try to load data into the empty structure!")

        dataLoader.loadData().forEach({ a, b ->
            val acc = this.getAccountByID(a)
            if(!b.equals(0L)){
                if(acc == null)
                    throw Exception("the account id '$a' missing!" )
                else {
                    acc.decimalPrecision = dataLoader.getDecimalPrecision()
                    acc.displayValue = b
                }
            }
        })

        return this
    }

    override fun toJSON():String{
        return CollectionToString.mkJSON(structure)
    }

    override fun toString(): String {
        return CollectionToString.mkString(structure)
    }
}