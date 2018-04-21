package eu.qiou.aaf4k.util

// https://gist.github.com/cy6erGn0m/97ecdc7191364572a94a
fun <K, V> Map<K, V>.mergeReduce(other: Map<K, V>, reduce: (V, V) -> V): Map<K, V> {
    val result = LinkedHashMap<K, V>(this.size + other.size)
    result.putAll(this)
    other.forEach { e -> result[e.key] = result[e.key]?.let { reduce(e.value, it) } ?: e.value }
    return result
}

fun Iterable<Any>.mkString(separator: String = ", ", prefix: String = "[", affix: String = "]") = eu.qiou.aaf4k.util.strings.CollectionToString.mkString(this, separator, prefix, affix)