package eu.qiou.aaf4k.util.template

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.io.ExcelUtil

class SimpleOverview(
        data: List<Map<String, *>> = listOf(),
        entityName: String = GlobalConfiguration.DEFAULT_REPORTING_ENTITY.name,
        projectName: String = GlobalConfiguration.DEFAULT_PROJECT_NAME,
        workingPaperName: String = "",
        processedBy: String = GlobalConfiguration.DEFAULT_PROCESSOR_NAME,
        theme: Template.Theme? = Template.Theme.DEFAULT
) :
        WorkingPaper(listOf(
                Template.HeadingFormat(value = "Nr.", formatData = ExcelUtil.DataFormat.STRING.format, isAutoIncrement = true),
                Template.HeadingFormat(value = "Name", formatData = ExcelUtil.DataFormat.STRING.format, columnWidth = 48),
                Template.HeadingFormat(value = "Summe", formatData = ExcelUtil.DataFormat.NUMBER.format,
                        dataAggregatable = true, columnWidth = 48)
        ), data, entityName, projectName, workingPaperName, processedBy, theme)