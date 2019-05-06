package eu.qiou.aaf4k.util

// base is the sorted list
class SortedList<E>(base: List<E> = listOf()) : MutableList<E> where E : Comparable<E> {

    private var list: MutableList<E> = base.toMutableList()

    fun mergeWithInPlace(other: SortedList<E>) {
        list = mergeWith(other)
    }

    fun mergeWith(other: SortedList<E>): SortedList<E> {
        val res = mutableListOf<E>()

        val left = this.size
        val right = other.size

        var i = 0
        var j = 0

        while (i < left && j < right) {
            res.add(if (this[i] <= other[j]) this[i++] else other[j++])
        }

        while (i < left) res.add(this[i++])
        while (j < right) res.add(other[j++])

        return SortedList(res)
    }

    override val size: Int
        get() = list.size

    override fun contains(element: E): Boolean {
        return indexOf(element) >= 0
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return elements.all { contains(it) }
    }

    override fun get(index: Int): E = list[index]

    override fun indexOf(element: E): Int {
        return list.binarySearch { element.compareTo(it) }
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): MutableIterator<E> = list.iterator()

    override fun lastIndexOf(element: E): Int = size - 1

    override fun add(element: E): Boolean {
        tailrec fun search(low: Int, high: Int): Int {
            val mid = (low + high) / 2
            val midVal = list[mid]

            return when {
                high - low == 1 -> if (element < list[low]) low else high
                element > midVal -> search(mid, high)
                else -> search(low, mid)
            }
        }
        if (size == 0)
            list.add(element)
        else {
            list.add(search(0, list.size), element)
        }
        return true
    }

    override fun add(index: Int, element: E) {
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        throw Exception("the order is predefined. cannot specify the index")
    }

    override fun addAll(elements: Collection<E>): Boolean {
        elements.forEach { add(it) }
        return true
    }

    override fun clear() {
        list.clear()
    }

    override fun listIterator(): MutableListIterator<E> = list.listIterator()

    override fun listIterator(index: Int): MutableListIterator<E> = list.listIterator(index)

    override fun remove(element: E): Boolean = list.remove(element)

    override fun removeAll(elements: Collection<E>): Boolean = list.removeAll(elements)

    override fun removeAt(index: Int): E = list.removeAt(index)

    override fun retainAll(elements: Collection<E>): Boolean = list.retainAll(elements)

    override fun set(index: Int, element: E): E {
        throw Exception("the order is predefined. cannot specify the index")
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = list.subList(fromIndex, toIndex)

    override fun toString(): String {
        return mkString()
    }
}