package eu.qiou.aaf4k.reportings.model

interface Drilldownable{

    fun getChildren():Collection<Drilldownable>?

    fun getParents():Collection<Drilldownable>?

    fun add(child: Drilldownable, index: Int? = null): Drilldownable

    fun remove(child: Drilldownable): Drilldownable

    fun addAll(children: Collection<Drilldownable>, index: Int? = null): Drilldownable {
        children.forEach {
            add(it, index)
        }

        return this
    }

    // find the direct parents
    fun findParentsRecursively(child: Drilldownable, res: MutableSet<Drilldownable> = mutableSetOf()): MutableSet<Drilldownable> {
        this.getChildren()?.let {
            it.fold(res) { b, a ->
                if (child.equals(a)) {
                    b.add(this)
                } else {
                    a.findParentsRecursively(child, b)
                }
                b
            }
        }
        return res
    }

    fun removeRecursively(child: Drilldownable):Drilldownable{
        this.findParentsRecursively(child).forEach {
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

    fun flattenIncludeSelf(): MutableList<Drilldownable> {
        val res: MutableList<Drilldownable> = mutableListOf(this)

        getChildren()?.forEach { a ->
            res.addAll(a.flattenIncludeSelf())
        }

        return res
    }

    fun countRecursively(includeSelf: Boolean = false): Int {
        val init = if (includeSelf) 1 else 0
        if(hasChildren()){
            return this.getChildren()!!.fold(init) { a, e ->
                a + when{
                    e.hasChildren() -> e.countRecursively(includeSelf)
                    else -> 1
                }
            }
        }
        return init
    }

    fun levels(): Int {
        if (hasChildren()) {
            return 1 + getChildren()!!.map { it.levels() }.maxBy { it }!!
        }

        return 1
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
        return findParentsRecursively(child).count() > 0
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