package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.template.DevelopmentOfAccount
import eu.qiou.aaf4k.util.template.Template
import org.junit.Test

class DevelopmentOfAccountTest {

    @Test
    fun test() {
        DevelopmentOfAccount(listOf(
                mapOf("Nr." to 1, "Name" to "Demo1", "Anfangsbestand" to 100, "Zugang" to 20, "Abgang" to 9, "Umbuchung" to 0, "Endebestand" to 45),
                mapOf("n" to 1, "Name" to "Demo1", "Anfangsbestand" to 100, "Abgang" to 20, "Zugang" to 9, "Umbuchung" to 0, "Endebestand" to 45),
                mapOf("Nr." to 1, "Name" to "Demo1", "Anfangsbestand" to 100, "Zugang" to 20, "Abgang" to 9, "Umbuchung" to 0, "Endebestand" to 45)
        ), "Demo GmbH",

                "Jahresabschlussprüfung 2018",
                "Rückstellungen",
                "Qiou Yang", theme = Template.Theme.BLACK_WHITE)
                .build("data/trail.xls")
    }
}