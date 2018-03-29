package eu.qiou.aaf4k.reportings

open class ProtoReporting(val id:Int, val name: String, var desc: String="",var accounts: Set<ProtoAccount>? = null,
                     var reportingInfo: ProtoReportingInfo = ProtoReportingInfo()) {
    fun getAccountByID(id: Int): ProtoAccount?{
        return accounts?.find { it.id == id }
    }
}