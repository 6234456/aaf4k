package eu.qiou.aaf4k.accounting.model

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.reportings.model.ProtoEntity
import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.template.Template
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import java.util.*


/**
 * @desc the adjustment of local reporting to group reporting is nothing short of the restructuring of the atomic
 *       accounts. In the case of transfer between GKV an UKV, the payroll-cost of each category ( operational, financial, distribution )
 *       should be available as separate account.
 */
class ReportingPackage(targetReportingTmpl: Reporting,
                       val intercompanyAccountPolicy: ((ProtoEntity, Account) -> InterCompanyPolicy?)? = null
) {

    private val components: MutableMap<ProtoEntity, Reporting> = mutableMapOf()

    val targetReporting = targetReportingTmpl.clone().apply { clearCategories(); prepareConsolidation() }
    val id: Int = targetReporting.id
    val name: String = targetReporting.name
    val desc: String = targetReporting.desc
    val structure: List<Account> = targetReporting.structure
    val group: ProtoEntity = targetReporting.entity
    val timeParameters: TimeParameters = targetReporting.timeParameters
    val currencyUnit: CurrencyUnit = targetReporting.displayUnit as CurrencyUnit

    fun localReportingOf(localReporting: Reporting, translator: ReportingTranslator? = null, locale: Locale = Locale.getDefault()): Reporting {
        with(
                if (translator == null) localReporting.clone()
                else translator.translate(localReporting, targetReporting)
        ) {
            clearCategories()
            prepareReclAdj(locale)
            components[this.entity] = this
            return this
        }
    }

    fun carryForward(targetReportingPackage: ReportingPackage): ReportingPackage {
        if (targetReporting.consCategoriesAdded) {
            with(targetReportingPackage.targetReporting) {
                if (!this.consCategoriesAdded)
                    this.prepareConsolidation()

                (targetReporting.categories as List<Category>)
                        .find { it.consolidationCategory == ConsolidationCategory.INIT_EQUITY }!!.deepCopy(this)

                (targetReporting.categories as List<Category>)
                        .find { it.consolidationCategory == ConsolidationCategory.SUBSEQUENT_EQUITY }!!.deepCopy(this)


                val re = targetReportingPackage.targetReporting.retainedEarning!!.id
                (targetReporting.categories as List<Category>)
                        .find { it.consolidationCategory == ConsolidationCategory.UNREALIZED_PROFIT_AND_LOSS }!!
                        .deepCopy(this).let {
                            it.entries.forEach { e ->
                                e.accounts.removeIf {
                                    it.reportingType == ReportingType.REVENUE_GAIN
                                            ||
                                            it.reportingType == ReportingType.EXPENSE_LOSS
                                }

                                (e as Entry).balanceWith(re)
                            }
                        }
            }
        }

        return targetReportingPackage
    }

    fun eliminateIntercompanyTransactions() {
        if (intercompanyAccountPolicy == null) {
            throw Exception("IC-AccountPolicy should be specified first.")
        }

        // srcEntity, targEntity, type
        val tmp = components.map {
            it.key to it.value.flattened.map { x ->
                intercompanyAccountPolicy.invoke(it.key, x)
            }.filter { x ->
                x != null
            }.groupBy { x ->
                x!!.targetEntity
            }.map { y ->
                y.key to (y.value as List<InterCompanyPolicy>).groupBy { x -> x.type }
            }.toMap()
        }.toMap()

        InterCompanyPolicy.eliminate(tmp, targetReporting)
    }

    fun toXl(
            path: String,
            t: Template.Theme = Template.Theme.DEFAULT,
            locale: Locale = GlobalConfiguration.DEFAULT_LOCALE
    ) {
        val shtNameOverview = "Overview"
        val shtNameAdjustments = "Adjustments"
        val colStart = 2
        var cnt = 0

        val data: MutableMap<Int, Map<Long, String>> = mutableMapOf()

        targetReporting.prepareConsolidation(locale)
        targetReporting.toXl(path, t, locale, shtNameOverview, shtNameAdjustments, components)

        components.forEach { k, v ->
            val overview = "${String.format("%03d", k.id)}_${k.abbreviation}_$shtNameOverview"
            val adj = "${String.format("%03d", k.id)}_${k.abbreviation}_$shtNameAdjustments"
            val (_, data1) = v.toXl(path, t, locale, overview, adj)
            data[colStart + cnt++] = data1
        }

        val (sht, _) = ExcelUtil.getWorksheet(path, sheetName = shtNameOverview)

        data.forEach { i, d ->
            ExcelUtil.unload(d, { if (ExcelUtil.digitRegex.matches(it)) it.toDouble().toLong() else -1 }, 0, i, { false }, { c, v ->
                c.cellFormula = v
            }, sht)
        }

        ExcelUtil.saveWorkbook(path, sht.workbook)

    }
}

enum class ConsolidationCategory(val token: Int) {
    INIT_EQUITY(0),
    SUBSEQUENT_EQUITY(1),
    PAYABLES_RECEIVABLES(2),
    UNREALIZED_PROFIT_AND_LOSS(3),
    REVENUE_EXPENSE(4)
}