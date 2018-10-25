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
                ?: "null"}, "address":${address?.toJSON() ?: "null"}, "child":${childEntitis?.mkJSON()
                ?: "null"}, "parent": ${parentEntitis?.mkJSON() ?: "null"}}"""
    }

    var childEntitis: MutableList<ProtoEntity>? = null
    var parentEntitis: MutableList<ProtoEntity>? = null

    override fun getChildren(): Collection<ProtoEntity>? {
        return childEntitis
    }

    override fun getParents(): Collection<ProtoEntity>? {
        return parentEntitis
    }

    override fun add(child: Drilldownable): ProtoEntity {
        if(child is ProtoEntity){
            if (childEntitis == null){
                childEntitis = mutableListOf()
            }
            childEntitis!!.add(child)

            if(child.parentEntitis == null){
                child.parentEntitis = mutableListOf()
            }
            child.parentEntitis!!.add(this)
        }

        return this
    }

    override fun remove(child: Drilldownable): ProtoEntity {
        if(child is ProtoEntity){
            if(childEntitis != null){
                if(childEntitis!!.contains(child)){
                    child.parentEntitis!!.remove(this)
                    childEntitis!!.remove(child)
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