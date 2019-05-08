package eu.qiou.aaf4k.reportings.base

/**
 *  top-down
 */
interface ProtoCollectionAccount : ProtoAccount, Drilldownable<ProtoCollectionAccount, ProtoAccount> {
    val subAccounts: MutableList<ProtoAccount>

    override var value: Long
        get() = if (subAccounts.isEmpty()) 0L else subAccounts.map { if (it.isStatistical) 0 else it.value }
                .reduce { acc, l -> acc + l }
        set(value) {
            throw Exception("set $value to the CollectionAccount $name is not forbidden.")
        }

    override fun getChildren(): Collection<ProtoAccount> = subAccounts

    override fun getParents(): Collection<ProtoCollectionAccount>? = superAccounts

    override fun add(child: ProtoAccount, index: Int?): ProtoCollectionAccount {
        //do nothing if child already exists and this account is not statistical account
        if (!isStatistical && child in this)
            return this

        if (index == null)
            subAccounts.add(child)
        else
            subAccounts.add(index, child)

        child.superAccounts.add(this)
        return super.add(child, index)
    }

    override fun remove(child: ProtoAccount): ProtoCollectionAccount {
        if (child in this) {
            subAccounts.remove(child)
            child.superAccounts.remove(this)
            super.remove(child)
        }
        return this
    }

    override fun copyWith(value: Double, decimalPrecision: Int): ProtoAccount {
        throw Exception("Applicable only to the atomic accounts.")
    }

    override fun nullify(): ProtoAccount {
        return (deepCopy() as ProtoCollectionAccount).apply {
            addAll(subAccounts.map { it.nullify() })
        }
    }
}