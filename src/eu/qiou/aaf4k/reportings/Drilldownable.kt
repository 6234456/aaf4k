package eu.qiou.aaf4k.reportings

interface Drilldownable {

    fun <T>getChildren():Collection<T>?

    fun <T>getParent():T?

    fun <T>hasChildren():Boolean{
        if(getChildren<T>() == null)
            return false
        else
            return getChildren<T>()!!.count() > 0
    }

    fun <T>hasParent():Boolean{
        return getParent<T>() == null
    }

}