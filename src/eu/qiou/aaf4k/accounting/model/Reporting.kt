package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.reportings.model.ProtoCategory
import eu.qiou.aaf4k.reportings.model.ProtoEntity
import eu.qiou.aaf4k.reportings.model.ProtoEntry
import eu.qiou.aaf4k.reportings.model.ProtoReporting
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit
import java.util.*

open class Reporting(id: Int, name: String, desc: String = "", structure: List<Account>,
                     displayUnit: ProtoUnit = CurrencyUnit(), entity: ProtoEntity = GlobalConfiguration.DEFAULT_ENTITY,
                     timeParameters: TimeParameters = GlobalConfiguration.DEFAULT_TIME_PARAMETERS)
    : ProtoReporting<Account>(id, name, desc, structure, displayUnit, entity, timeParameters) {

    private var consCategoriesAdded = false

    override fun copyCategoriesFrom(reporting: ProtoReporting<Account>) {
        (reporting.categories as Collection<Category>).forEach { it.deepCopy(this) }
    }

    override fun update(data: Map<Int, Double>, updateMethod: (Double, Double) -> Double): Reporting {
        return Reporting(id, name, desc,
                structure.map { it.deepCopy<Account>(data, updateMethod) }
                , displayUnit, entity, timeParameters).apply {
            this@Reporting.categories.forEach { it.deepCopy(this) }
        }
    }

    override fun update(entry: ProtoEntry<Account>, updateMethod: (Double, Double) -> Double): Reporting {
        return Reporting(id, name, desc,
                structure.map { it.deepCopy(entry, updateMethod) }
                , displayUnit, entity, timeParameters).apply {
            this@Reporting.categories.forEach { it.deepCopy(this) }
        }
    }

    override fun update(category: ProtoCategory<Account>, updateMethod: (Double, Double) -> Double): Reporting {
        return Reporting(id, name, desc,
                structure.map { it.deepCopy(category, updateMethod) }
                , displayUnit, entity, timeParameters).apply {
            this@Reporting.categories.forEach { it.deepCopy(this) }
        }
    }

    override fun updateStructure(method: (List<Account>) -> List<Account>): Reporting {
        return Reporting(id, name, desc,
                method(structure)
                , displayUnit, entity, timeParameters).apply {
            copyCategoriesFrom(this@Reporting)
        }
    }

    override fun addAccountTo(newAccount: Account, index: Int, parentId: Int?): Reporting {
        if (parentId == null)
            return updateStructure { structure.toMutableList().apply { add(index, newAccount) } }

        return Reporting(id, name, desc,
                structure.map { it.deepCopy { x: Account -> x } }
                , displayUnit, entity, timeParameters).apply {
            val p = findAccountByID(parentId)

            p ?: throw java.lang.Exception("No account found for the id: $parentId.")

            if (p.isAggregate)
                p.add(newAccount, index)

            copyCategoriesFrom(this@Reporting)
        }
    }

    override fun removeAccount(accountId: Int): Reporting {
        val p = findAccountByID(accountId)
        p ?: throw java.lang.Exception("No account found for the id: $accountId.")

        if (p.hasParent()) {
            return Reporting(id, name, desc,
                    structure.map { it.deepCopy { x: Account -> x } }
                    , displayUnit, entity, timeParameters).apply {

                p.superAccounts!!.forEach {
                    it.remove(p)
                }

                copyCategoriesFrom(this@Reporting)
            }
        } else {
            return updateStructure { structure.toMutableList().apply { remove(p) } }
        }
    }

    override fun shorten(): Reporting {
        val whiteList = categories.fold(flattened) { acc, protoCategory ->
            acc + protoCategory.flatten(true)
        }.filter { it.value != 0L }.toSet()

        return Reporting(id, name, desc,
                structure.map { it.shorten(whiteList = whiteList) as Account }
                , displayUnit, entity, timeParameters).apply {
            copyCategoriesFrom(this@Reporting)
        }
    }

    val retainedEarning = flattened.find { it.reportingType == ReportingType.RESULT_BALANCE }
    val oci = flattened.find { it.reportingType == ReportingType.PROFIT_LOSS_NEUTRAL_BALANCE }

    fun addConsolidationCategories(locale: Locale? = null) {
        if (!consCategoriesAdded) {
            val nid = lastCategoryIndex()

            val msg = if (locale == null)
                ResourceBundle.getBundle("aaf4k")
            else
                ResourceBundle.getBundle("aaf4k", locale)

            Category(msg.getString("erstKons"), nid, msg.getString("erstKons"), this)
            Category(msg.getString("folgKons"), nid + 1, msg.getString("folgKons"), this)
            Category(msg.getString("schuKons"), nid + 2, msg.getString("schuKons"), this)
            Category(msg.getString("aeKons"), nid + 3, msg.getString("aeKons"), this)
            Category(msg.getString("zwischenGewinnE"), nid + 4, msg.getString("zwischenGewinnE"), this)

            consCategoriesAdded = true
        }

    }

}