package eu.qiou.aaf4k.reportings

/**
 *  the reporting Entity
 *  can be a department, a cost center, a subsidiary, a product line or a conglomerate
 */

data class ProtoEntity(val id: Int, var name: String, var abbreviation: String = name, var desc:String = "") {

    var hasChildEntity: Boolean = false
    get() = childEntitis.count() > 0

    var hasParentEntity: Boolean = false
    get() = parentEntity != null

    var parentEntity: ProtoEntity? = null
    val childEntitis: MutableSet<ProtoEntity> = mutableSetOf()

    fun add(child: ProtoEntity):ProtoEntity{
        childEntitis.add(child)
        child.parentEntity = this

        return this
    }

    operator fun plusAssign(child: ProtoEntity){
        this.add(child)
    }

    operator fun plus(child: ProtoEntity) = this.add(child)

    fun remove(child: ProtoEntity):ProtoEntity{
        childEntitis.remove(child)
        return this
    }

    operator fun minusAssign(child: ProtoEntity){
        this.remove(child)
    }

    operator fun minus(child: ProtoEntity) = this.remove(child)

    operator fun contains(child: ProtoEntity) = childEntitis.contains(child)


    override fun equals(other: Any?): Boolean {
        if( other is ProtoEntity){
            return other.id == this.id
        }
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "($id $name)"
    }
}