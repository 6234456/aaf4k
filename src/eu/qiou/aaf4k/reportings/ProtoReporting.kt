package eu.qiou.aaf4k.reportings

class ProtoReporting(val name: String, var desc: String="",var accounts: List<ProtoAccount>? = null,
                     var reportingInfo: ProtoReportingInfo = ProtoReportingInfo()) {

}