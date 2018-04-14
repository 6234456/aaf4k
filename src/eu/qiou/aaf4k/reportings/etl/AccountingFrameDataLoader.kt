package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.ProtoUnit

interface AccountingFrameDataLoader:DataLoader {
    fun loadAccountingFrameData():MutableMap<Int, Pair<String, Double>>

    fun parseAccount(entry: Map.Entry<Int, Pair<String, Double>>, displayUnit: ProtoUnit = CurrencyUnit()): ProtoAccount
}