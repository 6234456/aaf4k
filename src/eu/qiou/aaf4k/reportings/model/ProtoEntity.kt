package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mkJSON
import eu.qiou.aaf4k.util.strings.CollectionToString

/**
 *  the reporting Entity
 *  can be a department, a cost center, a subsidiary, a product line or a conglomerate
 */

data class ProtoEntity(val id: Int, var name: String, var abbreviation: String = name, var desc:String = "",
                       var contactPerson: Person? = null, var address: Address? = null) : Drilldownable, JSONable {
    override fun toJSON(): String {
        return """{"id":$id, "name":"$name", "abbreviation":"$abbreviation", "desc":"$desc", "contactPerson":${contactPerson?.toJSON()
                ?: "null"}, "address":${address?.toJSON()
                ?: "null"}, "child":${(childEntities as Map<JSONable, Double>?)?.mkJSON()
                ?: "null"}}"""
    }

    var childEntities: MutableMap<ProtoEntity, Double>? = null
    var parentEntities: MutableMap<ProtoEntity, Double>? = null

    override fun getChildren(): Collection<ProtoEntity>? {
        return childEntities?.keys
    }

    override fun getParents(): Collection<ProtoEntity>? {
        return parentEntities?.keys
    }

    // 100% to 10000   18.23% to 1823
    override fun add(child: Drilldownable, index: Int?): ProtoEntity {
        if(child is ProtoEntity){
            if (childEntities == null) {
                childEntities = mutableMapOf()
            }

            childEntities!!.put(child, if (index == null) 1.0 else index / 10000.0)

            if (child.parentEntities == null) {
                child.parentEntities = mutableMapOf()
            }

            child.parentEntities!!.put(this, if (index == null) 1.0 else index / 10000.0)
        }

        return this
    }

    override fun remove(child: Drilldownable): ProtoEntity {
        if(child is ProtoEntity){
            if (childEntities != null) {
                if (childEntities!!.contains(child)) {
                    child.parentEntities!!.remove(this)
                    childEntities!!.remove(child)
                }
            }
        }

        return this
    }

    override fun equals(other: Any?): Boolean {
        if( other is ProtoEntity){
            return other.id == this.id
        }
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun subEntities(): Map<ProtoEntity, Double> {
        val res: MutableMap<ProtoEntity, Double> = mutableMapOf()

        if (this.hasChildren()) {
            val e = this.childEntities!!
            return e.keys.fold(res) { acc, protoEntity ->
                val r = this.childEntities!![protoEntity]!!
                val f = protoEntity.subEntities().map { it.key to it.value * r }.toMap()

                if (acc.containsKey(protoEntity))
                    acc[protoEntity] = acc[protoEntity]!! + r
                else
                    acc.put(protoEntity, r)

                f.forEach { k, v ->
                    if (acc.containsKey(k)) {
                        acc[k] = acc[k]!! + v
                    } else {
                        acc.put(k, v)
                    }
                }

                acc
            }
        }

        return res
    }

    fun shareOf(entity: ProtoEntity): Double {
        return subEntities().getOrDefault(entity, 0.0)
    }

    private fun upper():String{
        return "[ $name ]"
    }

    private fun lower():String{
        return "( $name )"
    }

    override fun toString(): String {
        return CollectionToString.structuredToStr(this, 0, ProtoEntity::lower as Drilldownable.() -> String, ProtoEntity::upper as Drilldownable.() -> String,
                trappings = { parent, child ->
                    (parent as ProtoEntity)
                    (child as ProtoEntity)
                    String.format("%3.2f", parent.childEntities?.get(child)!! * 100) + "% "
                }
        )
    }
}