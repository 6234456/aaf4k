package eu.qiou.aaf4k

import eu.qiou.aaf4k.util.ProtoUnit

class ProtoAccount(val id: Int, val name: String, var value:Long, var unit: ProtoUnit, var desc: String=""){
    var hasSuperAccount: Boolean = false
    var decimalPrecision: Int = 2
}