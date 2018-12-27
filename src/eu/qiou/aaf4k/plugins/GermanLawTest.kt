package eu.qiou.aaf4k.plugins

import eu.qiou.aaf4k.util.mkString
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class GermanLawTest {

    @Test
    fun law() {
        println(GermanLaw.law())
    }

    @Test
    fun walk() {
        Files.write(
                Paths.get("data/law.txt"),
                GermanLaw.walk().map { it.first + "|||" + it.second.mkString("###", "", "") }
        )
    }
}