package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.Amtsgericht
import eu.qiou.aaf4k.plugins.Handelsregister
import org.junit.Test

class HandelsregisterTest {

    @Test
    fun get() {
        val l = Handelsregister.get("daimler AG", Amtsgericht.STUTTGART, 3)
        println(l.size)
        println(l)
    }

    @Test
    fun collect() {
        val l = Handelsregister.walk("Ehrfeld", Amtsgericht.MAINZ)
        println(l.size)
        println(l)
    }
}