package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.reportings.model.ProtoEntity
import eu.qiou.aaf4k.util.template.Template
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import java.util.*


/**
 * @desc the adjustment of local reporting to group reporting is nothing short of the restructuring of the atomic
 *       accounts. In the case of transfer between GKV an UKV, the payroll-cost of each category ( operational, financial, distribution )
 *       should be available as separate account.
 */
class ReportingPackage(private val targetReporting: Reporting) {

    private val components: MutableMap<ProtoEntity, Reporting> = mutableMapOf()

    val id: Int = targetReporting.id
    val name: String = targetReporting.name
    val desc: String = targetReporting.desc
    val structure: List<Account> = targetReporting.structure
    val group: ProtoEntity = targetReporting.entity
    val timeParameters: TimeParameters = targetReporting.timeParameters
    val currencyUnit: CurrencyUnit = targetReporting.displayUnit as CurrencyUnit

    fun localReportingOf(localReporting: Reporting, translator: ReportingTranslator? = null, locale: Locale = Locale.getDefault()) {
        with(
                if (translator == null) localReporting
                else translator.translate(localReporting, targetReporting)
        ) {
            prepareReclAdj(locale)
            components.put(this.entity, this)
        }
    }

    fun toXl(
            path: String,
            t: Template.Theme = Template.Theme.DEFAULT,
            locale: Locale = GlobalConfiguration.DEFAULT_LOCALE
    ) {
        val shtNameOverview = "Overview"
        val shtNameAdjustments = "Adjustments"

        targetReporting.prepareConsolidation(locale)
        targetReporting.toXl(path, t, locale, shtNameOverview, shtNameAdjustments, components)

        components.forEach { k, v ->
            v.toXl(path, t, locale,
                    "${String.format("%03d", k.id)}_${k.abbreviation}_$shtNameOverview",
                    "${String.format("%03d", k.id)}_${k.abbreviation}_$shtNameAdjustments")
        }
    }
}