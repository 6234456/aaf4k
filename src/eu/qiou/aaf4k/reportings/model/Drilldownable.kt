package eu.qiou.aaf4k.reportings.model

interface Drilldownable{

    fun getChildren():Collection<Drilldownable>?

    fun getParents():Collection<Drilldownable>?

    fun add(child: Drilldownable): Drilldownable

    fun remove(child: Drilldownable): Drilldownable

    fun findParentRecursively(child: Drilldownable, res: MutableSet<Drilldownable> = mutableSetOf()): MutableSet<Drilldownable> {
        this.getChildren()!!.fold(res){
            b, a ->
                if(child.equals(a)) {
                    b.add(this)
                } else  {
                    a.findParentRecursively(child, b)
                }
            b
        }
        return res
    }

    fun removeRecursively(child: Drilldownable):Drilldownable{
        this.findParentRecursively(child).forEach {
            it.remove(child)
        }

        return this
    }

    fun hasParent():Boolean{
        val parents = getParents()
        return parents != null && parents.count() > 0
    }

    fun hasChildren():Boolean{
        val children = getChildren()
        return children != null && children.count() > 0
    }

    fun flatten(): MutableList<Drilldownable> {
        val res : MutableList<Drilldownable> = mutableListOf()

        if (hasChildren())
            getChildren()!!.forEach { a ->
                if (a.hasChildren()) {
                    res.addAll(a.flatten())
                } else {
                    res.add(a)
                }
            }

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
        return findParentRecursively(child).count() > 0
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