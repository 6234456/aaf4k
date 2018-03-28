package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.ProtoAccount

class AggregateAccount(id:Int, name:String, var accounts: MutableSet<ProtoAccount> = mutableSetOf<ProtoAccount>(), desc:String=""):ProtoAccount(
        id=id, name=name, desc=desc) {

    override var value = 0L
    get() = if(accounts == null) 0 else accounts?.fold(0L){a , b ->  a + b.value}!!

    fun addSubAccount(account: ProtoAccount){
        accounts.add(account)
    }

}