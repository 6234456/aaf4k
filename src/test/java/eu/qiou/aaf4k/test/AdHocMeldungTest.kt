package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.AdHocMeldung
import org.junit.Test

class AdHocMeldungTest {

    @Test
    fun existsCompany() {
        println(AdHocMeldung.meldung("daimler"))
    }
}