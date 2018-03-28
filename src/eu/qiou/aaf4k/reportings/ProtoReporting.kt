package eu.qiou.aaf4k.reportings

import eu.qiou.aaf4k.ProtoAccount

class ProtoReporting(val name: String, var desc: String="",var accounts: List<ProtoAccount>? = null,
                     var reportingInfo: ProtoReportingInfo = ProtoReportingInfo()) {

}