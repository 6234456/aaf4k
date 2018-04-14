package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.model.ProtoEntity
import eu.qiou.aaf4k.util.strings.times
import org.junit.Test

class ProtoEntityTest {

    @Test
    fun toStringTest() {
        val e1 = ProtoEntity(0, "qiou Holding AG", "AG")
        val e2 = ProtoEntity(1, "qiou Programming GmbH", "GmbH")
        val e3 = ProtoEntity(2, "qiou Programming2 GmbH", "GmbH2")
        val e4 = ProtoEntity(3, "qiou Programming3 GmbH", "GmbH3")

        e1.add(e2)
        e1.add(e3)

        e2.add(e4)

        println(e1)
        println("^*" * 20)

        e2.remove(e4)
        println(e1)

    }
}