package eu.qiou.aaf4k.util

import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.strings.CollectionToString

// https://gist.github.com/cy6erGn0m/97ecdc7191364572a94a
fun <K, V> Map<K, V>.mergeReduce(other: Map<K, V>, reduce: (V, V) -> V): Map<K, V> {
    val result = LinkedHashMap<K, V>(this.size + other.size)
    result.putAll(this)
    other.forEach { e -> result[e.key] = result[e.key]?.let { reduce(e.value, it) } ?: e.value }
    return result
}

fun Iterable<Any>.mkString(separator: String = ", ", prefix: String = "[", affix: String = "]") = eu.qiou.aaf4k.util.strings.CollectionToString.mkString(this, separator, prefix, affix)


fun Iterable<JSONable>.mkJSON() = CollectionToString.mkJSON(this)

fun Map<JSONable, Number>.mkJSON() = CollectionToString.mkString(this.map { """{"key":${it.key.toJSON()}, "value": ${it.value} }""" })
/**
 * @sample   to [1,2,3,4]  -> [k, 1, 2, 3, 4]
 */
fun Map<*, *>.flatList(): List<List<*>> {
    return this.keys.map { e ->
        with(this[e]!!) {
            when {
                this is Iterable<*> ->
                    mutableListOf(e).let { l -> l + this }
                else ->
                    mutableListOf(e, this)
            }
        }
    }
}

fun <R, T> Iterable<T>.foldTrackList(initial: R, operation: (R, T, Int) -> R): List<R> {
    var tmp = initial

    return this.mapIndexed { i, e ->
        tmp = operation(tmp, e, i)
        tmp
    }
}

fun <R, T> Iterable<T>.foldTrackListInit(initial: R, operation: (R, T, Int) -> R): List<R> {

    return listOf(initial) + this.foldTrackList(initial, operation)

}


fun <R, T> Iterable<T>.groupNearby(operation: (T) -> R): List<List<T>> {
    val cnt = this.count()
    var tmp = mutableListOf<T>()
    return with(this.map { t -> operation(t) }) {
        this.foldIndexed(mutableListOf<List<T>>()) { index, acc, r ->
            tmp.add(this@groupNearby.elementAt(index))
            if (index < cnt - 1) {
                if (r != this[index + 1]) {
                    acc.add(tmp)
                    tmp = mutableListOf<T>()
                }
            } else {
                acc.add(tmp)
            }
            acc
        }
    }
}


fun <R> Iterable<R>.reduceTrackList(operation: (R, R, Int) -> R): List<R> = this.foldTrackList(this.elementAt(0), operation)

fun <K, V, R> Map<K, V>.replaceValueBasedOnIndex(iterable: Iterable<R>): Map<K, R> {
    return this.keys.zip(iterable).toMap()
}

fun <K, V, R> Map<K, V>.mapValuesIndexed(operation: (Map.Entry<K, V>, Int) -> R): Map<K, R> {
    var cnt = 0
    return this.mapValues { operation(it, cnt++) }
}

fun <K, V, R> Map<K, V>.mapKeysIndexed(operation: (Map.Entry<K, V>, Int) -> R): Map<R, V> {
    var cnt = 0
    return this.mapKeys { operation(it, cnt++) }
}

fun <K, V, R> Iterable<R>.replaceValueBasedOnIndex(map: Map<K, V>): Map<K, R> = map.replaceValueBasedOnIndex(this)

fun <K> Iterable<K>.toIndexedMap(): Map<Int, K> = this.mapIndexed { i, k -> i to k }.toMap()
