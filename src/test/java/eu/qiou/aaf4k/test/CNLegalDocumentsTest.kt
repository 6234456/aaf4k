package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.CNLegalDocuments
import org.junit.Test

class CNLegalDocumentsTest {

    @Test
    fun search() {
        CNLegalDocuments().search("上海大众")
    }
}