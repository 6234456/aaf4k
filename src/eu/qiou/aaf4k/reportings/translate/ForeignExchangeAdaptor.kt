package eu.qiou.aaf4k.reportings.translate

import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.util.io.FxProvider
import eu.qiou.aaf4k.util.time.TimeParameters

interface ForeignExchangeAdaptor {
    fun translate(account: ProtoAccount, timeParameters: TimeParameters, provider: FxProvider): ProtoAccount
}