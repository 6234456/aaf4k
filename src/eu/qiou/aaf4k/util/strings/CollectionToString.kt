package eu.qiou.aaf4k.util.strings

import eu.qiou.aaf4k.reportings.model.Drilldownable
import eu.qiou.aaf4k.util.io.JSONable

object CollectionToString {
    fun mkString(iterable : Iterable<Any>, separator:String = ", ", prefix:String = "[", affix:String = "]"):String{
        return makeString(Any::toString, iterable, separator, prefix, affix)
    }

    fun mkJSON(iterable : Iterable<JSONable>, separator:String = ", ", prefix:String = "[", affix:String = "]"):String{
        return makeString(JSONable::toJSON, iterable, separator, prefix, affix)
    }

    private fun <T>makeString(f:T.()->String, iterable : Iterable<T>, separator:String = ", ", prefix:String = "[", affix:String = "]"):String{
        return prefix + iterable.fold(""){a, b -> a + (if(a.isEmpty()) "" else separator) + b.f() } + affix
    }

    fun structuredToStr(drilldownable: Drilldownable, level:Int = 0, asSingleToStr: Drilldownable.()->String, asParentToStr:  Drilldownable.()->String):String{
           if(! drilldownable.hasChildren())
               return drilldownable .asSingleToStr()
           else{
               return "\t"* level + drilldownable.asParentToStr() + ":{\n" + drilldownable.getChildren()!!.fold(""){
                   acc: String, childType -> acc +
                        (
                            if(childType.hasChildren())
                                structuredToStr(childType, level+1, asSingleToStr, asParentToStr)
                            else
                                "\t" * (level + 1) + childType.toString()
                        )+ "\n"
               } + "\t"* level + "}"
           }
    }
}