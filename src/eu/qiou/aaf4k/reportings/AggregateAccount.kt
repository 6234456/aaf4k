package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.util.strings.CollectionToString
import eu.qiou.aaf4k.util.strings.StringUtil.repeatString

class AggregateAccount(id:Int, name:String, val accounts: MutableSet<ProtoAccount> = mutableSetOf<ProtoAccount>(), desc:String=""):ProtoAccount(
        id=id, name=name, desc=desc, hasSubAccounts = true) {

    override var value = 0L
    get() =  accounts.fold(0L){a , b ->  a + b.value}


    operator fun contains(account: ProtoAccount):Boolean {
        return accounts.contains(account)
    }

    operator fun plus(account: ProtoAccount): AggregateAccount{
        addSubAccount(account)
        return this
    }

    operator fun minus(account: ProtoAccount): AggregateAccount{
        removeRecursiveSubAccount(account)
        return this
    }

    fun addSubAccount(account: ProtoAccount){
        account.hasSuperAccounts = true
        account.superAccount = this

        accounts.add(account)
    }

    fun removeRecursiveSubAccount(account: ProtoAccount): Boolean{
        this.accounts
                .forEach{
                            a -> if(account.equals(a)){
                                    if(a is AggregateAccount) {
                                        a.removeRecursiveSubAccount(account)
                                    }
                                    else {
                                        accounts.remove(a)
                                        return true
                                    }
                                }
                        }

        return false
    }

    fun checkDistinct(): MutableSet<ProtoAccount> {
        val res : MutableSet<ProtoAccount> = mutableSetOf()
        val flat : MutableList<ProtoAccount> = flatten()

        flat.forEachIndexed { index, protoAccount ->  if(index > 0){
                                    if(flat[index-1].equals(protoAccount)){
                                        res.add(protoAccount)
                                    }
                                }
                            }
        return res
    }

    fun flatten(sorted:Boolean = true):MutableList<ProtoAccount>{

        val res : MutableList<ProtoAccount> = mutableListOf()
        this.accounts.forEach{
            a -> if(a is AggregateAccount) {
                res.addAll(a.flatten())
            }
            else {
                res.add(a)
            }
        }

        if(sorted) res.sortBy { it.id }

        return res
    }

    fun count():Int{
        return this.accounts.fold(0) { a, e ->
            a + when{
                e is AggregateAccount ->  e.count()
                else -> 1
            }
        }
    }

    // TODO("Extract the recursive toString to util.io")
    override fun toString(): String {
        return _toString()
    }

    private fun _toString(lvl:Int=0):String{
        return repeatString(lvl) +"[$id $name]: {\n" +
                accounts.fold("") { a, b -> a +
                        if(b is AggregateAccount)
                            b._toString(lvl+1) + "\n"
                        else
                            repeatString(lvl+1) + b.toString() + "\n "} +
                repeatString(lvl) +"}"
    }

    override fun toJSON():String{
        return "{id: $id, name: '$name', value: $value, displayValue: $displayValue, decimalPrecision: $decimalPrecision, desc: '$desc', hasSubAccounts: $hasSubAccounts, hasSuperAccounts: $hasSuperAccounts, localAccountID: $localAccountID, subAccounts: " +
                CollectionToString.mkJSON(accounts, ",\n")  + "}"

    }

}