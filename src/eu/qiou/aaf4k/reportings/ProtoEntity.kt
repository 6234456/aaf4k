package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.util.strings.CollectionToString

/**
 *  the reporting Entity
 *  can be a department, a cost center, a subsidiary, a product line or a conglomerate
 */

data class ProtoEntity(val id: Int, var name: String, var abbreviation: String = name, var desc:String = "",
                       var contactPerson: Person? = null, var address: Address? = null): Drilldownable<ProtoEntity, ProtoEntity> {

    var childEntitis: MutableSet<ProtoEntity> ? = null
    var parentEntitis: MutableSet<ProtoEntity> ? = null

    override fun getChildren(): Collection<ProtoEntity>? {
        return childEntitis
    }

    override fun getParent(): Collection<ProtoEntity>? {
        return parentEntitis
    }

    override fun add(child: ProtoEntity):ProtoEntity{
        if (childEntitis == null){
            childEntitis = mutableSetOf()
        }
        childEntitis!!.add(child)

        if(child.parentEntitis == null){
            child.parentEntitis = mutableSetOf()
        }
        child.parentEntitis!!.add(this)

        return this
    }

    override fun remove(child: ProtoEntity):ProtoEntity{
        if(childEntitis != null){
            if(childEntitis!!.contains(child)){
                child.parentEntitis!!.remove(this)
                childEntitis!!.remove(child)
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

    private fun upper():String{
        return "[ $name ]"
    }

    private fun lower():String{
        return "( $name )"
    }

    override fun toString(): String {
        return CollectionToString.structuredToStr(this, 0, ProtoEntity::lower , ProtoEntity::upper )
    }
}