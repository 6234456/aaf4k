package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.CNLegalDocuments
import org.junit.Test
import java.util.*

class CNLegalDocumentsTest {

    @Test
    fun search() {
        CNLegalDocuments.search(listOf("大众", "上海"))
    }

    @Test
    fun trail() {
        val a = UUID.randomUUID().toString()
        println(a.take(18) + a.drop(19))
    }
}