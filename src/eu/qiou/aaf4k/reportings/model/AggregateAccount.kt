package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.strings.CollectionToString

class AggregateAccount(id:Int, name:String, val accounts: MutableSet<ProtoAccount> = mutableSetOf(), desc:String=""): ProtoAccount(
        id=id, name=name, desc=desc), Drilldownable<ProtoAccount, AggregateAccount> {

    override var value = 0L
    get() =  accounts.fold(0L){a , b ->  a + b.value}

    override fun getParent(): Collection<AggregateAccount>? {
        return superAccounts
    }

    override fun  getChildren(): Collection<ProtoAccount>? {
        return accounts
    }

    override fun add(account: ProtoAccount): AggregateAccount {
        account.register(this)
        accounts.add(account)
        return this
    }

    override fun remove(account: ProtoAccount): AggregateAccount {
        account.unregister(this)
        accounts.remove(account)
        return this
    }

    fun checkDuplicated(): MutableSet<ProtoAccount> {
        val res : MutableSet<ProtoAccount> = mutableSetOf()
        val flat : MutableList<ProtoAccount> = flatten(true, {this.id})

        flat.forEachIndexed { index, protoAccount ->  if(index > 0){
                                    if(flat[index-1].equals(protoAccount)){
                                        res.add(protoAccount)
                                    }
                                }
                            }
        return res
    }

    private fun titel():String{
        return "[$id $name]"
    }

    override fun toString(): String {
        return CollectionToString.structuredToStr(this, 0, ProtoAccount::toString, AggregateAccount::titel)
    }

    override fun toJSON():String{
        return "{id: $id, name: '$name', value: $value, displayValue: $displayValue, decimalPrecision: $decimalPrecision, desc: '$desc', hasSubAccounts: $hasSubAccounts, hasSuperAccounts: $hasSuperAccounts, localAccountID: $localAccountID, subAccounts: " +
                CollectionToString.mkJSON(accounts, ",\n")  + "}"
    }

}