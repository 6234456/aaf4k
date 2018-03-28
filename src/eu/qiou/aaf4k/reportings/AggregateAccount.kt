package eu.qiou.aaf4k.reportings

class AggregateAccount(id:Int, name:String, var accounts: MutableSet<ProtoAccount> = mutableSetOf<ProtoAccount>(), desc:String=""):ProtoAccount(
        id=id, name=name, desc=desc) {

    override var value = 0L
    get() =  accounts.fold(0L){a , b ->  a + b.value}

    fun addSubAccount(account: ProtoAccount){

        account.hasSuperAccounts = true
        account.superAccount = this

        accounts.add(account)
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

    fun flatten():MutableList<ProtoAccount>{

        val res : MutableList<ProtoAccount> = mutableListOf()
        this.accounts.forEach{
            a -> if(a is AggregateAccount) {
                res.addAll(a.flatten())
            }
            else {
                res.add(a)
            }
        }

        res.sortBy { it.id }

        return res
    }

    override fun toString(): String {
        return _toString()
    }

    private fun _toString(lvl:Int=0):String{
        return repeatString(lvl) +"["+ id + " " + name + "]: {\n" +
                accounts.fold("") { a, b -> a +
                        if(b is AggregateAccount)
                            b._toString(lvl+1) + "\n"
                        else
                            repeatString(lvl+1) + b.toString() + "\n "} +
                repeatString(lvl) +"}"
    }

    private fun repeatString(times: Int, token: String ="\t"):String{
        if(times <= 0) return ""
        else return token + repeatString(times - 1)
    }

}