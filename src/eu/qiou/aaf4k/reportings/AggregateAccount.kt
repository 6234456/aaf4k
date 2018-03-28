package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.ProtoAccount
import java.util.*

class AggregateAccount(id:Int, name:String, var accounts: MutableSet<ProtoAccount> = mutableSetOf<ProtoAccount>(), desc:String=""):ProtoAccount(
        id=id, name=name, desc=desc) {

    override var value = 0L
    get() = if(accounts == null) 0 else accounts?.fold(0L){a , b ->  a + b.value}!!

    fun addSubAccount(account: ProtoAccount){
        if(accounts.contains(account)){
            throw Exception("Account Duplicated: " + account.toString())
        }

        if(this.equals(account)){
            throw Exception("Can not add itself!")
        }
        accounts.add(account)
    }

    override fun toString(): String {
        return _toString()
    }

    private fun _toString(lvl:Int=0):String{
        return repeatString(lvl) +"["+ id + " " + name + "]: {\n" + accounts.fold("") { a, b -> a + if(b is AggregateAccount) b._toString(lvl+1) else repeatString(lvl+1) + b.toString()  + "\n "} + "\n"+ repeatString(lvl+1) +"}"
    }

    private fun repeatString(times: Int, token: String ="\t"):String{
        if(times <= 0) return ""
        else return token + repeatString(times - 1)
    }

}