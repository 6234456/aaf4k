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
                ?: "null"}, "address":${address?.toJSON() ?: "null"}, "child":${childEntities?.mkJSON()
                ?: "null"}, "parent": ${parentEntities?.mkJSON() ?: "null"}}"""
    }

    var childEntities: MutableList<ProtoEntity>? = null
    var parentEntities: MutableList<ProtoEntity>? = null

    override fun getChildren(): Collection<ProtoEntity>? {
        return childEntities
    }

    override fun getParents(): Collection<ProtoEntity>? {
        return parentEntities
    }

    override fun add(child: Drilldownable, index: Int?): ProtoEntity {
        if(child is ProtoEntity){
            if (childEntities == null) {
                childEntities = mutableListOf()
            }
            if (index == null)
                childEntities!!.add(child)
            else
                childEntities!!.add(index, child)

            if (child.parentEntities == null) {
                child.parentEntities = mutableListOf()
            }
            child.parentEntities!!.add(this)
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

    private fun upper():String{
        return "[ $name ]"
    }

    private fun lower():String{
        return "( $name )"
    }

    override fun toString(): String {
        return CollectionToString.structuredToStr(this, 0, ProtoEntity::lower as Drilldownable.() -> String , ProtoEntity::upper as Drilldownable.() -> String )
    }
}