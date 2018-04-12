package eu.qiou.aaf4k.reportings

interface Drilldownable<ChildType, ParentType> where ParentType: ChildType{

    fun getChildren():Collection<ChildType>?

    fun getParent():Collection<ParentType>?

    fun add(child: ChildType):Drilldownable<ChildType, ParentType>

    fun remove(child: ChildType):Drilldownable<ChildType, ParentType>

    @Suppress("UNCHECKED_CAST")
    fun findRecursively(child: ChildType, res: MutableSet<ParentType> = mutableSetOf()):MutableSet<ParentType>{
        this.getChildren()!!.fold(res){
            b, a ->
                if(child!!.equals(a)) {
                    b.add(this as ParentType)
                } else if(a is Drilldownable<*, *>) {
                    (a as Drilldownable<ChildType, ParentType>).findRecursively(child, b)
                }
            b
        }
        return res
    }

    @Suppress("UNCHECKED_CAST")
    fun removeRecursively(child: ChildType):ParentType{
        this.findRecursively(child).forEach{
            (it as Drilldownable<ChildType, ParentType>).remove(child)
        }

        return this as ParentType
    }

    @Suppress("UNCHECKED_CAST")
    fun flatten(sorted:Boolean = true, sortBy: ChildType.() -> Int):MutableList<ChildType>{
        val res : MutableList<ChildType> = mutableListOf()
        this.getChildren()!!.forEach{
            a -> if(a is Drilldownable<*,*>) {
                    res.addAll((a as Drilldownable<ChildType, ParentType>).flatten(false, sortBy))
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
                    e is Drilldownable<*,*> ->  e.countRecursively()
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


    fun hasChildren():Boolean{
        if(getChildren() == null)
            return false
        else
            return getChildren()!!.count() > 0
    }

    fun hasParent():Boolean{
        return getParent() == null
    }

    operator fun contains(child:ChildType):Boolean {
        if(!hasChildren()){
            return false
        }
        return findRecursively(child).count() > 0
    }

    operator fun plusAssign(child: ChildType){
        this.add(child)
    }

    operator fun plus(child: ChildType) = this.add(child)

    operator fun minusAssign(child: ChildType){
        this.remove(child)
    }

    operator fun minus(child: ChildType) = this.remove(child)

}