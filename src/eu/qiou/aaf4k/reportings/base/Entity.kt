package eu.qiou.aaf4k.reportings.base

import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mkJSON
import eu.qiou.aaf4k.util.strings.CollectionToString

/**
 *  the reporting Entity
 *  can be a department, a cost center, a subsidiary, a product line or a conglomerate
 */

data class Entity(override val id: Long, var name: String, var abbreviation: String = name, var desc: String = "",
                  var contactPerson: Person? = null, var address: Address? = null) : Drilldownable<Entity, Entity>, JSONable, Identifiable {

    @Suppress("UNCHECKED_CAST")
    override fun toJSON(): String {
        return """{"id":$id, "name":"$name", "abbreviation":"$abbreviation", "desc":"$desc", "contactPerson":${contactPerson?.toJSON()
                ?: "null"}, "address":${address?.toJSON()
                ?: "null"}, "child":${if (hasChildren()) (childEntities as Map<JSONable, Double>).mkJSON()
        else "null"}}"""
    }

    val childEntities: MutableMap<Entity, Double> = mutableMapOf()
    private val parentEntities: MutableMap<Entity, Double> = mutableMapOf()

    override fun getChildren(): Collection<Entity> {
        return childEntities.keys
    }

    override fun getParents(): Collection<Entity>? {
        return parentEntities.keys
    }

    override var toUpdate: Boolean = false
    override var cacheList: List<Entity> = listOf()
    override var cacheAllList: List<Entity> = listOf()

    // 100% to 10000   18.23% to 1823
    override fun add(child: Entity, index: Int?): Entity {
        childEntities.put(child, if (index == null) 1.0 else index / 10000.0)
        child.parentEntities.put(this, if (index == null) 1.0 else index / 10000.0)

        return this
    }

    override fun remove(child: Entity): Entity {
        if (childEntities.contains(child)) {
            child.parentEntities.remove(this)
            childEntities.remove(child)
        }

        return this
    }

    override fun isEqual(one: Entity, other: Entity): Boolean {
        return one == other
    }

    override fun equals(other: Any?): Boolean {
        if (other is Entity) {
            return other.id == this.id
        }
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun subEntities(): Map<Entity, Double> {
        val res: MutableMap<Entity, Double> = mutableMapOf()

        if (this.hasChildren()) {
            val e = this.childEntities
            return e.keys.fold(res) { acc, protoEntity ->
                val r = this.childEntities[protoEntity]!!
                val f = protoEntity.subEntities().map { it.key to it.value * r }.toMap()

                if (acc.containsKey(protoEntity))
                    acc[protoEntity] = acc[protoEntity]!! + r
                else
                    acc[protoEntity] = r

                f.forEach { (k, v) ->
                    if (acc.containsKey(k)) {
                        acc[k] = acc[k]!! + v
                    } else {
                        acc[k] = v
                    }
                }

                acc
            }
        }

        return res
    }

    fun findChildBy(judgement: (Entity) -> Boolean, subEntity: Map<Entity, Double>? = null): Entity? {
        if (judgement(this))
            return this

        return (subEntity ?: subEntities()).keys.find { judgement(it) }
    }

    fun shareOf(entity: Entity): Double {
        return subEntities().getOrDefault(entity, 0.0)
    }

    private fun upper(): String {
        return "[ $name ]"
    }

    private fun lower(x: Int): String {
        return "( $name )"
    }

    @Suppress("UNCHECKED_CAST")
    override fun toString(): String {
        return CollectionToString.structuredToStr(this, 0, Entity::lower as Entity.(Int) -> String, Entity::upper as Entity.() -> String,
                trappings = { parent, child ->
                    (parent as Entity)
                    String.format("%3.2f", parent.childEntities.get(child)!! * 100) + "% "
                }
        )
    }
}

enum class EntityType {
    LIMITED_LIABILITY,
    LIMITED_LIABILITY_SHARE_BASED,
    PARTNERSHIP_SHARE_BASED,
    PARTNERSHIP_NORMAL,
    PARTNERSHIP_LIMITED
}