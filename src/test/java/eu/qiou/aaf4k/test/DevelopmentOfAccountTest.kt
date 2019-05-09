package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.template.DevelopmentOfAccount
import eu.qiou.aaf4k.util.template.Template
import org.junit.Test

class DevelopmentOfAccountTest {

    @Test
    fun test() {
        DevelopmentOfAccount(listOf(
                mapOf( "Name" to "Demo1", "Anfangsbestand" to 100, "Zugang" to 20, "Abgang" to 9, "Umbuchung" to 0),
                mapOf("Name" to "Demo1", "Anfangsbestand" to 100, "Abgang" to 20, "Zugang" to 9, "Umbuchung" to 0),
                mapOf( "Name" to "Demo1", "Anfangsbestand" to 100, "Zugang" to 20, "Abgang" to 9, "Umbuchung" to 0)
        ), "Demo GmbH",

                "JAP 2018",
                "Rückstellungen",
                "Yang", theme = Template.Theme.BLACK_WHITE).also {
            it.entityName = "Trail GmbH"
        }
                .build("data/trail.xlsx")

        DevelopmentOfAccount(listOf(
                mapOf("Name" to "Demo1", "Anfangsbestand" to 100, "Zugang" to 20, "Abgang" to 9, "Umbuchung" to 0),
                mapOf("Name" to "Demo1", "Anfangsbestand" to 100, "Abgang" to 20, "Zugang" to 9, "Umbuchung" to 0),
                mapOf("Name" to "Demo1", "Anfangsbestand" to 100, "Zugang" to 20, "Abgang" to 9, "Umbuchung" to 0)
        ), "Demo GmbH",

                "JAP 2018",
                "Rückstellungen",
                "Qiou Yang", theme = Template.Theme.ORANGE)
                .build("data/trail.xls")
    }
}