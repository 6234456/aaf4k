package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.template.*
import org.junit.Test


class HierarchyTest {

    @Test
    fun hierarchyTest1() {

        WorkingPaper.gloablProcessor = "WinH"
        WorkingPaper.globalTheme = Template.Theme.LAVANDA

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
                document("Review ARAP", SimpleOverview::class) {
                    data = listOf(
                            mapOf("Name" to "Demo1", "Summe" to 32320),
                            mapOf("Name" to "Demo1", "Summe" to 32320), mapOf("Name" to "Demo1", "Summe" to 32320),
                            mapOf("Name" to "Demo1", "Summe" to 32320), mapOf("Name" to "Demo1", "Summe" to 32320),
                            mapOf("Name" to "Demo1", "Summe" to 32320), mapOf("Name" to "Demo1", "Summe" to 32320),
                            mapOf("Name" to "Demo1", "Summe" to 32320),
                            mapOf("Name" to "Demo1", "Summe" to 32320)
                    )
                    entityName = "Demo AG"
                }
                hierarchy("sub3") {
                    hierarchy("sub4") {

                    }
                }
            }
        }.generate()
    }
}