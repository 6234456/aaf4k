package eu.qiou.aaf4k.util

import eu.qiou.aaf4k.ProtoAccount

interface ProtoUnit{
    fun format(account: ProtoAccount):String

    fun decs():String
}