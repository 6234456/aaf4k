package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.reportings.ProtoAccount
import eu.qiou.aaf4k.reportings.ProtoReporting
import eu.qiou.aaf4k.util.time.TimeParameters

/**
 * in some cases, the account data info is unknown, but the structure is pre-defined based on, for example, the account id.
 * As to the SKR3 of the German accounting, the accounts with an id ranging from 8400 to 8900 belongs to category the revenue.
 * When the definition of the new accounts follows the given pattern, i.e. regulation of the accounting frame, there exists no need to pre-define the whole structure in advance.
 *
 * the orginially loosely coupled data and structure are combined in the accounting frame
 */

abstract class AccountingFrame(id: Int, name: String):ProtoReporting(id, name, timeParameters = TimeParameters()) {

    abstract fun addAccount(account:ProtoAccount)

    override fun loadData(dataLoader: DataLoader): ProtoReporting {
        if(! (dataLoader is AccountingFrameDataLoader) ){
            throw Exception("")
        }

        dataLoader.loadAccountingFrameData().forEach({ it ->
            val acc = dataLoader.parseAccount(it, this.displayUnit)
            this.addAccount(acc)
        })

        return this
    }
}