package eu.qiou.aaf4k.reportings.model

interface Drilldownable{

    fun getChildren():Collection<Drilldownable>?

    fun getParent():Collection<Drilldownable>?

    fun add(child: Drilldownable): Drilldownable

    fun remove(child: Drilldownable): Drilldownable

    fun findRecursively(child: Drilldownable, res: MutableSet<Drilldownable> = mutableSetOf()):MutableSet<Drilldownable>{
        this.getChildren()!!.fold(res){
            b, a ->
                if(child.equals(a)) {
                    b.add(this)
                } else  {
                    a.findRecursively(child, b)
                }
            b
        }
        return res
    }

    fun removeRecursively(child: Drilldownable):Drilldownable{
        this.findRecursively(child).forEach{
            it.remove(child)
        }

        return this
    }

    fun hasParent():Boolean{
        return getParent() == null
    }

    fun hasChildren():Boolean{
        if(getChildren() == null)
            return false
        else
            return getChildren()!!.count() > 0
    }

    fun flatten(sorted:Boolean = true, sortBy: Drilldownable.() -> Int):MutableList<Drilldownable>{
        val res : MutableList<Drilldownable> = mutableListOf()
        this.getChildren()!!.forEach{
            a -> if(a.hasChildren()) {
                    res.addAll(a.flatten(false, sortBy))
                }
                else {
                    res.add(a)
                }
        }

        if(sorted) res.sortBy { it.sortBy() }
        return res
    }

    fun countRecursively():Int{
        if(hasChildren()){
            return this.getChildren()!!.fold(0) { a, e ->
                a + when{
                    e.hasChildren() ->  e.countRecursively()
                    else -> 1
                }
            }
        }
        return 0
    }

    fun count():Int {
        if(hasChildren())
            return getChildren()!!.count()

        return 0
    }


    operator fun contains(child:Drilldownable):Boolean {
        if(!hasChildren()){
            return false
        }
        return findRecursively(child).count() > 0
    }

    operator fun plusAssign(child: Drilldownable){
        this.add(child)
    }

    operator fun plus(child: Drilldownable) = this.add(child)

    operator fun minusAssign(child: Drilldownable){
        this.remove(child)
    }

    operator fun minus(child: Drilldownable) = this.remove(child)

}