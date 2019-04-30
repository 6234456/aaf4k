package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.template.DevelopmentOfAccount
import eu.qiou.aaf4k.util.template.Template
import eu.qiou.aaf4k.util.template.WorkingPaper
import eu.qiou.aaf4k.util.template.hierarchy
import org.junit.Test


class HierarchyTest {

    @Test
    fun hierarchyTest1() {

        WorkingPaper.gloablProcessor = "WinH"
        WorkingPaper.globalTheme = Template.Theme.LAVENA

        hierarchy("trail") {
            document("Prüfung Rückstellungen", DevelopmentOfAccount::class) {
                data = listOf(
                        mapOf("Name" to "Demo1", "Anfangsbestand" to 100, "Zugang" to 20, "Abgang" to 9, "Umbuchung" to 0),
                        mapOf("Name" to "Demo1", "Anfangsbestand" to 100, "Abgang" to 20, "Zugang" to 9, "Umbuchung" to 0),
                        mapOf("Name" to "Demo1", "Anfangsbestand" to 100, "Zugang" to 20, "Abgang" to 9, "Umbuchung" to 0)
                )
                processedBy = "WindHunter"
                entityName = "Trail AG"
            }
            hierarchy("sub2") {
                document("Review ARAP", DevelopmentOfAccount::class) {
                    data = listOf(
                            mapOf("Name" to "Demo1", "Anfangsbestand" to 100, "Zugang" to 20, "Abgang" to 9, "Umbuchung" to 0),
                            mapOf("Name" to "Demo1", "Anfangsbestand" to 100, "Abgang" to 20, "Zugang" to 9, "Umbuchung" to 0),
                            mapOf("Name" to "Demo1", "Anfangsbestand" to 100, "Zugang" to 20, "Abgang" to 9, "Umbuchung" to 0)
                    )
                    entityName = "Trail AG"
                }
                hierarchy("sub3") {
                    hierarchy("sub4") {

                    }
                }
            }
        }.generate()
    }
}