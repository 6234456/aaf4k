package eu.qiou.aaf4k.util.template

import eu.qiou.aaf4k.util.io.ExcelUtil

class DevelopmentOfAccount(data: List<Map<String, *>>, entityName: String, projectName: String, workingPaperName: String, processedBy: String, theme: Theme? = Theme.BLOOD) : Template(
        headings = listOf(
                HeadingFormat(value = "Nr.", formatData = ExcelUtil.DataFormat.STRING.format, isAutoIncrement = true),
                HeadingFormat(value = "Name", formatData = ExcelUtil.DataFormat.STRING.format),
                HeadingFormat(value = "Anfangsbestand", formatData = ExcelUtil.DataFormat.NUMBER.format, dataAggregatable = true),
                HeadingFormat(value = "Zugang", formatData = ExcelUtil.DataFormat.NUMBER.format, dataAggregatable = true),
                HeadingFormat(value = "Abgang", formatData = ExcelUtil.DataFormat.NUMBER.format, dataAggregatable = true),
                HeadingFormat(value = "Umbuchung", formatData = ExcelUtil.DataFormat.NUMBER.format, dataAggregatable = true),
                HeadingFormat(value = "Endebestand", formatData = ExcelUtil.DataFormat.NUMBER.format, dataAggregatable = true, formula = "[-1]-[-2]+[-3]+[-4]")
        ),
        theme = theme,
        data = data,
        caption = listOf(entityName to projectName, workingPaperName to "$processedBy/${java.time.LocalDate.now()}"),
        sumRowBottom = HeadingFormat("Summe")
)