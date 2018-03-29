package eu.qiou.aaf4k.reportings.accounting

import eu.qiou.aaf4k.reportings.ProtoReporting
import eu.qiou.aaf4k.util.TimeAttribute
import eu.qiou.aaf4k.util.TimeSpan

class ReportingPackage(val timeSpan: TimeSpan, var desc:String = "") {
    var reportings: MutableSet<ProtoReporting> = mutableSetOf()

    fun addComponent(reporting : ProtoReporting){
        when(reporting.reportingInfo.timeAttribute){
             TimeAttribute.TIME_POINT -> reporting.reportingInfo.timePoint = timeSpan.end
             else -> reporting.reportingInfo.timeSpan = timeSpan
        }

        reportings.add(reporting)
    }
}