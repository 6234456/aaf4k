package eu.qiou.aaf4k.util.template

import eu.qiou.aaf4k.reportings.GlobalConfiguration

abstract class WorkingPaper(private val headings: List<Template.HeadingFormat>,
                            var data: List<Map<String, *>> = listOf(),
                            var entityName: String = GlobalConfiguration.DEFAULT_REPORTING_ENTITY.name,
                            private var projectName: String = GlobalConfiguration.DEFAULT_PROJECT_NAME,
                            var workingPaperName: String = "",
                            var processedBy: String = GlobalConfiguration.DEFAULT_PROCESSOR_NAME,
                            var theme: Template.Theme? = Template.Theme.DEFAULT) {
    companion object {
        var globalTheme: Template.Theme? = null
        var globalEntityName: String? = null
        var globalProjectName: String? = null
        var gloablProcessor: String? = null
    }

    fun build(path: String, sheetName: String = "Overview") {
        Template(
                headings = headings,
                theme = globalTheme ?: theme,
                data = data,
                caption = listOf((globalEntityName ?: entityName) to (globalProjectName
                        ?: projectName), workingPaperName to "${gloablProcessor
                        ?: processedBy}/${java.time.LocalDate.now()}"),
                sumRowBottom = Template.HeadingFormat("Summe")
        ).build(path, sheetName)
    }
}