package eu.qiou.aaf4k.util.strings

import eu.qiou.aaf4k.util.io.JSONable

object CollectionToString {
    fun mkString(iterable : Iterable<Any>, separator:String = ", ", prefix:String = "[", affix:String = "]"):String{
        return prefix + iterable.fold(""){a, b -> a + (if(a.length == 0) "" else separator) + b.toString() } + affix
    }

    fun mkJSON(iterable : Iterable<JSONable>, separator:String = ", ", prefix:String = "[", affix:String = "]"):String{
        return return prefix + iterable.fold(""){a, b -> a + (if(a.length == 0) "" else separator) + b.toJSON() } + affix
    }

    /*
     TODO(unify the two methods with functional programming)
     */
    private fun buildString(iterable : Iterable<Any>, separator:String = ", ", prefix:String = "[", affix:String = "]", f:Any.()->String = Any::toString):String{
        return prefix + iterable.fold(""){a, b -> a + (if(a.length == 0) "" else separator) + with(b){ f() } } + affix
    }
}