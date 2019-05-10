package eu.qiou.aaf4k.reportings.base

import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.template.Template
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import java.util.*

class ReportingPackage(targetReportingTmpl: Reporting,
                       private val intercompanyAccountPolicy: ((Entity, ProtoAccount) -> InterCompanyPolicy?)? = null
) {

    private val components: MutableMap<Entity, Reporting> = mutableMapOf()

    val targetReporting = (targetReportingTmpl.deepCopy() as Reporting).apply { clearCategories(); prepareConsolidation() }
    val id: Long = targetReporting.id
    val name: String = targetReporting.name
    val desc: String = targetReporting.desc
    val group: Entity = targetReporting.entity
    val timeParameters: TimeParameters = targetReporting.timeParameters
    val currencyUnit: CurrencyUnit = targetReporting.displayUnit as CurrencyUnit

    fun localReportingOf(localReporting: Reporting, translator: ReportingTranslator? = null, locale: Locale = Locale.getDefault()): Reporting {
        with(
                if (translator == null) localReporting.deepCopy() as Reporting
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

                this.categories.removeIf {
                    it.consolidationCategory == ConsolidationCategory.INIT_EQUITY ||
                            it.consolidationCategory == ConsolidationCategory.SUBSEQUENT_EQUITY ||
                            it.consolidationCategory == ConsolidationCategory.UNREALIZED_PROFIT_AND_LOSS
                }


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

                                e.balanceWith(re)
                            }
                        }
            }
        }

        return targetReportingPackage
    }

    @Suppress("UNCHECKED_CAST")
    fun eliminateIntercompanyTransactions() {
        if (intercompanyAccountPolicy == null) {
            throw Exception("IC-AccountPolicy should be specified first.")
        }

        // srcEntity, targEntity, type
        val tmp = components.map {
            it.key to it.value.sortedList().map { x ->
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
            locale: Locale = Locale.getDefault()
    ) {
        val shtNameOverview = "Overview"
        val shtNameAdjustments = "Adjustments"
        val colStart = 2
        var cnt = 0

        val data: MutableMap<Int, Map<Long, String>> = mutableMapOf()

        targetReporting.prepareConsolidation(locale)
        targetReporting.toXl(path, t, locale, shtNameOverview, shtNameAdjustments, components)

        components.forEach { (k, v) ->
            val overview = "${String.format("%03d", k.id)}_${k.abbreviation}_$shtNameOverview"
            val adj = "${String.format("%03d", k.id)}_${k.abbreviation}_$shtNameAdjustments"
            val (_, data1) = v.toXl(path, t, locale, overview, adj)
            data[colStart + cnt++] = data1
        }

        val (sht, _) = ExcelUtil.getWorksheet(path, sheetName = shtNameOverview)

        data.forEach { (i, d) ->
            ExcelUtil.unload(d, { if (ExcelUtil.digitRegex.matches(it)) it.toDouble().toLong() else -1 }, 0, i, { false }, { c, v ->
                c.cellFormula = v
            }, sht)
        }

        ExcelUtil.saveWorkbook(path, sht.workbook)

    }
}