package eu.qiou.aaf4k.reportings.accounting

import eu.qiou.aaf4k.reportings.model.ProtoReporting

class ReportingPackage {
    var reportings: MutableSet<ProtoReporting> = mutableSetOf()

    fun addComponent(reporting : ProtoReporting){
        reportings.add(reporting)
    }
}