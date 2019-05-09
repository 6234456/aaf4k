package eu.qiou.aaf4k.util.template

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import eu.qiou.aaf4k.util.io.ExcelUtil

class DevelopmentOfAccount(
        data: List<Map<String, *>> = listOf(),
        entityName: String = GlobalConfiguration.DEFAULT_REPORTING_ENTITY.name,
        projectName: String = GlobalConfiguration.DEFAULT_PROJECT_NAME,
        workingPaperName: String = "",
        processedBy: String = GlobalConfiguration.DEFAULT_PROCESSOR_NAME,
        theme: Template.Theme? = Template.Theme.BLOOD
) :
        WorkingPaper(listOf(
                Template.HeadingFormat(value = "Nr.", formatData = ExcelUtil.DataFormat.STRING.format, isAutoIncrement = true),
                Template.HeadingFormat(value = "Name", formatData = ExcelUtil.DataFormat.STRING.format),
                Template.HeadingFormat(value = "Anfangsbestand", formatData = ExcelUtil.DataFormat.NUMBER.format, dataAggregatable = true),
                Template.HeadingFormat(value = "Zugang", formatData = ExcelUtil.DataFormat.NUMBER.format, dataAggregatable = true),
                Template.HeadingFormat(value = "Abgang", formatData = ExcelUtil.DataFormat.NUMBER.format, dataAggregatable = true),
                Template.HeadingFormat(value = "Umbuchung", formatData = ExcelUtil.DataFormat.NUMBER.format, dataAggregatable = true),
                Template.HeadingFormat(value = "Endebestand", formatData = ExcelUtil.DataFormat.NUMBER.format, dataAggregatable = true, formula = "[-1]-[-2]+[-3]+[-4]")
        ), data, entityName, projectName, workingPaperName, processedBy, theme)
