package eu.qiou.aaf4k.util.template

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.time.TimeSpan
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ChronoOverview(
        timeSpan: TimeSpan = TimeSpan.forYear(LocalDate.now().year),
        data: Map<String, List<*>> = mapOf(),
        entityName: String = GlobalConfiguration.DEFAULT_REPORTING_ENTITY.name,
        projectName: String = GlobalConfiguration.DEFAULT_PROJECT_NAME,
        workingPaperName: String = "Entwicklung vom ${timeSpan.start} bis ${timeSpan.end}",
        processedBy: String = GlobalConfiguration.DEFAULT_PROCESSOR_NAME,
        theme: Template.Theme? = Template.Theme.DEFAULT
        ) :
        WorkingPaper(
                listOf(Template.HeadingFormat("Name")) + (timeSpan.getChildren().map {
                    Template.HeadingFormat(value = it.start.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                            formatData = ExcelUtil.DataFormat.NUMBER.format,
                            dataAggregatable = true, bindingKeyinData = it.toString(), columnWidth = 16
                    )
                }) + listOf(Template.HeadingFormat("Summe", dataAggregatable = true, formula = "SUM((2):[-1])")),
                data.map { entry -> timeSpan.getChildren().map { it.toString() }.zip(entry.value).toMap() + ("Name" to entry.key) },
                entityName, projectName, workingPaperName, processedBy, theme)