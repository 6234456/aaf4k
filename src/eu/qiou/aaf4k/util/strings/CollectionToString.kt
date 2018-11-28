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

    // trapping see Entity toString  the percentage
    fun structuredToStr(drilldownable: Drilldownable, level: Int = 0, asSingleToStr: Drilldownable.() -> String,
                        asParentToStr: Drilldownable.() -> String,
                        trappings: (Drilldownable, Drilldownable) -> String = { _, _ -> "" },
                        parent: Drilldownable? = null): String {
        val s = (if (parent != null) trappings(parent, drilldownable) else "")

        return "\t" * level + s + if (!drilldownable.hasChildren())
            drilldownable.asSingleToStr()
        else {
            drilldownable.asParentToStr() + ":{\n" + drilldownable.getChildren()!!.fold("") { acc: String, childType ->
                acc + structuredToStr(childType, level + 1, asSingleToStr, asParentToStr, trappings, drilldownable) + "\n"
            } + "\t" * level + "}"
        }
    }
}