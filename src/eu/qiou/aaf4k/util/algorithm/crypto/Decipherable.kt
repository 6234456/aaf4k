package eu.qiou.aaf4k.util.algorithm.crypto

interface Decipherable {
    fun encode(msg: String):List<Long>
    fun decode(i: List<Long>):String
}