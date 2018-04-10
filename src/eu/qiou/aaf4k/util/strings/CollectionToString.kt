package eu.qiou.aaf4k.util.strings

import eu.qiou.aaf4k.reportings.Drilldownable
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

    fun <ChildType, ParentType:ChildType>structuredToStr(drilldownable: Drilldownable<ChildType, ParentType>, level:Int = 0, asSingleToStr: ChildType.()->String, asParentToStr:  ParentType.()->String):String{
           if(! drilldownable.hasChildren())
               return (drilldownable as ChildType).asSingleToStr()
           else{
               return "\t"* level + (drilldownable as ParentType).asParentToStr() + ":{\n" + drilldownable.getChildren()!!.fold(""){
                   acc: String, childType: ChildType -> acc +
                        (
                            if(childType is Drilldownable<*,*> && childType.hasChildren())
                                structuredToStr(childType as Drilldownable<ChildType, ParentType>, level+1, asSingleToStr, asParentToStr)
                            else
                                "\t" * (level + 1) + childType.toString()
                        )+ "\n"
               } + "\t"* level + "}"
           }
    }
}