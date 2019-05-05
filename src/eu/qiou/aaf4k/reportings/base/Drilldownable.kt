package eu.qiou.aaf4k.reportings.base

/**
 *  typed interface Drilldownable
 *
 *  @property P :  the container class, like collection account, P is the class which implements the interface
 *  @property C:    the base class proto account
 */
interface Drilldownable<P, C> : Iterable<C> where P : C, C : Identifiable {

    fun getChildren(): Collection<C>

    fun getParents(): Collection<P>?

    override fun iterator(): Iterator<C> = flatten().iterator()

    fun add(child: C, index: Int? = null): P

    fun remove(child: C): P

    var sortedList: List<C>?
    var sortedAllList: List<C>?

    @Suppress("UNCHECKED_CAST")
    fun search(id: Long): C? {
        getChildren().forEach {
            if (it.id == id) return it
            if (it is Drilldownable<*, *>) return it.search(id) as C?
        }

        return null
    }

    fun binarySearch(id: Long, includeSelf: Boolean = false): C? {
        if (!includeSelf && sortedList == null) flatten()
        if (includeSelf && sortedAllList == null) flattenIncludeSelf()

        val l = if (includeSelf) sortedAllList else sortedList

        val tmp = (l?.binarySearch {
            when {
                it.id == id -> 0
                it.id < id -> -1
                else -> 1
            }
        }) ?: -1

        return if (tmp < 0) null else l?.get(tmp)

    }

    @Suppress("UNCHECKED_CAST")
    fun addAll(children: Collection<C>, index: Int? = null): P {
        children.forEach {
            add(it, index)
        }

        return this as P
    }

    fun isEqual(one: C, other: C): Boolean {
        return one.id == other.id
    }


    // find the direct parents
    @Suppress("UNCHECKED_CAST")
    fun findParentsRecursively(child: C, res: MutableSet<P> = mutableSetOf()): MutableSet<P> {
        getChildren().fold(res) { b, a ->
            if (isEqual(child, a)) {
                b.add(this as P)
            } else {
                if (a is Drilldownable<*, *>)
                    (a as Drilldownable<P, C>).findParentsRecursively(child, b)
            }
            b
        }

        return res
    }

    @Suppress("UNCHECKED_CAST")
    fun removeRecursively(child: C): P {
        this.findParentsRecursively(child).forEach {
            (it as Drilldownable<P, C>).remove(child)
        }

        return this as P
    }

    fun hasParent(): Boolean {
        val parents = getParents()
        return parents != null && parents.count() > 0
    }

    fun hasChildren(): Boolean {
        val children = getChildren()
        return children.count() > 0
    }

    // collection of the atomic accounts
    @Suppress("UNCHECKED_CAST")
    fun flatten(): MutableList<C> {
        val res: MutableList<C> = mutableListOf()

        if (hasChildren())
            getChildren().forEach { a ->
                if (a is Drilldownable<*, *>) {
                    res.addAll((a as Drilldownable<P, C>).flatten())
                } else {
                    res.add(a)
                }
            }

        sortedList = res.sortedBy { it.id }

        return res
    }

    @Suppress("UNCHECKED_CAST")
    fun flattenIncludeSelf(): MutableList<C> {
        val res: MutableList<C> = mutableListOf(this as C)

        getChildren().forEach { a ->
            res.addAll(if (a is Drilldownable<*, *>) (a as Drilldownable<P, C>).flattenIncludeSelf() else listOf(a))
        }

        sortedAllList = res

        return res
    }

    fun countRecursively(includeSelf: Boolean = false): Int {
        val init = if (includeSelf) 1 else 0
        if (hasChildren()) {
            return this.getChildren().fold(init) { a, e ->
                a + when (e) {
                    is Drilldownable<*, *> -> e.countRecursively(includeSelf)
                    else -> 1
                }
            }
        }
        return init
    }

    @Suppress("UNCHECKED_CAST")
    fun levels(): Int {
        return levels(this as C)
    }

    @Suppress("UNCHECKED_CAST")
    private fun levels(ele: C): Int {
        return when (ele) {
            is Drilldownable<*, *> -> (ele as Drilldownable<P, C>).getChildren().map { levels(it) }.maxBy { it } ?: 0
            else -> 1
        }
    }

    fun count(): Int {
        if (hasChildren())
            return getChildren().count()

        return 0
    }

    operator fun contains(child: C): Boolean {
        if (!hasChildren()) {
            return false
        }
        return findParentsRecursively(child).count() > 0
    }

    operator fun plusAssign(child: C) {
        this.add(child)
    }

    operator fun plus(child: C) = this.add(child)
    operator fun plus(child: Collection<C>) = this.addAll(child)

    operator fun minusAssign(child: C) {
        this.remove(child)
    }

    operator fun minus(child: C) = this.remove(child)
}