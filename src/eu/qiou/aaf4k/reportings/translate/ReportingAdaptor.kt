package eu.qiou.aaf4k.reportings.translate

import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.reportings.model.ProtoReporting


/**
 * Re-Mapping the structure of reporting
 * in case of Consolidation in German sense, i.e. the translate between Local GAAP to HB I of the group accounting
 * or the translation between financial and managerial accounting
 */
interface ReportingAdaptor<Source, Target, T : ProtoAccount> where Target : ProtoReporting<T>, Source : ProtoReporting<T> {
    fun mapping(input: Source): Target
}