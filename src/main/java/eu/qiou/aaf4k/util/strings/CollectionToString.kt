package eu.qiou.aaf4k.util.strings

import eu.qiou.aaf4k.reportings.base.Drilldownable
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

    @Suppress("UNCHECKED_CAST")
    fun <P : Drilldownable<*, *>, C> structuredToStr(
            drilldownable: C,
            level: Int = 0,
            asSingleToStr: C.(Int) -> String,
            asParentToStr: P.() -> String,
            trappings: (Drilldownable<*, *>, C) -> String = { _, _ -> "" },
            parent: P? = null): String {
        val s = (if (parent != null) trappings(parent as Drilldownable<*, *>, drilldownable) else "")

        return (("\t" * level) + s) +
                if ((drilldownable is Drilldownable<*, *> && drilldownable.hasChildren())) {
                    (drilldownable as P).asParentToStr() + " : {\n" + drilldownable.getChildren().fold("") { acc: String, childType ->
                        acc + structuredToStr(childType as C, level + 1, asSingleToStr, asParentToStr, trappings, drilldownable) + "\n"
                    } + "\t" * level + "}"
                } else {
                    drilldownable.asSingleToStr(level)
                }
    }

}