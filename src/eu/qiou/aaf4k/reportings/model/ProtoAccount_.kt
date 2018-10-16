package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.io.JSONable


/**
 * @author Qiou Yang
 * @since 1.0.0
 * @param id the internal id of the account
 * @param name the internal name of the account
 * @param unit specify the unit stored in the value
 *
 * @param isStatistical for aggregate account, will not be checked for duplicity
 */

//TODO simplify the base class only the necessary attributes   the foreign exchange into separate adaptor   ProtoAccount as Interface
interface ProtoAccount_ : JSONable, Drilldownable {

    val id: Int
    val name: String
    val subAccounts: Collection<ProtoAccount_>?
    val superAccounts: Collection<ProtoAccount_>?
    val decimalPrecision: Int
    val value: Long?
    val desc: String

    override fun getChildren(): Collection<ProtoAccount_>? {
        return subAccounts
    }

    override fun getParents(): Collection<ProtoAccount_>? {
        return superAccounts
    }

    fun hasSubAccounts(): Boolean = hasChildren()

    fun hasSuperAccounts(): Boolean = hasParent()

    fun isAggregate(): Boolean = subAccounts != null

    fun decimalValue(): Double {
        return if (isAggregate())
            subAccounts!!.fold(0.0) { acc, e ->
                acc + e.decimalValue()
            }
        else
            value!!.toDouble() / Math.pow(10.0, decimalPrecision.toDouble())
    }

    fun findChildByID(id: Int): ProtoAccount_? {
        if (this.id == id) {
            return this
        }

        subAccounts?.forEach {
            it.findChildByID(id)?.let {
                return it
            }
        }

        return null
    }
}