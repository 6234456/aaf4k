package eu.qiou.aaf4k.reportings.base

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit
import java.time.LocalDate

data class CollectionAccount(override val id: Long, override val name: String,
                             override val decimalPrecision: Int = GlobalConfiguration.DEFAULT_DECIMAL_PRECISION,
                             override val unit: ProtoUnit = CurrencyUnit(decimalPrecision = decimalPrecision),
                             override val desc: String = "",
                             override val timeParameters: TimeParameters? = null,
                             override val entity: Entity? = null,
                             override val isStatistical: Boolean = false,
                             override val validateUntil: LocalDate? = null,
                             override val reportingType: ReportingType = ReportingType.AUTO, override val displayUnit: ProtoUnit = unit
) : ProtoCollectionAccount {

    override val subAccounts: MutableList<ProtoAccount> = mutableListOf()
    override val superAccounts: MutableList<ProtoCollectionAccount> = mutableListOf()

    override fun deepCopy(): ProtoAccount {
        return copy(id = id).apply {
            subAccounts.clear()
            superAccounts.clear()
            this@CollectionAccount.subAccounts.forEach {
                this + it.deepCopy()
            }
        }
    }

    override var toUpdate = false

    override var cacheList: List<ProtoAccount> = listOf()
    override var cacheAllList: List<ProtoAccount> = listOf()


    override fun toString(): String = toStrings()

}