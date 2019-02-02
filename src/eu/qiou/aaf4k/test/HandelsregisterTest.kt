package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.Amtsgericht
import eu.qiou.aaf4k.plugins.Handelsregister
import org.junit.Test

class HandelsregisterTest {

    @Test
    fun get() {
        println(Handelsregister.get("bdo", Amtsgericht.HAMBURG))
    }
}