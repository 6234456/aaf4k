package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.base.Entity
import eu.qiou.aaf4k.util.io.toEntity
import org.junit.Test

class EntityTest {

    @Test
    fun toStringTest() {
        val e1 = Entity(0, "qiou Holding AG", "AG")
        val e2 = Entity(1, "qiou Programming GmbH", "GmbH")
        val e21 = Entity(21, "qiou Programming21 GmbH", "GmbH")
        val e22 = Entity(22, "qiou Programming22 GmbH", "GmbH")
        val e3 = Entity(2, "qiou Programming2 GmbH", "GmbH2")
        val e4 = Entity(3, "qiou Programming3 GmbH", "GmbH3")

        e1.add(e2, 8000)
        e1.add(e3, 1800)
        e1.add(e22, 5123)

        e2.add(e4, 9500)
        e2.add(e21)
        e2.add(e22, 5000)
        println(e1)
        println(e1.childEntities[e3])

        println(e1.toJSON().toEntity())
        println(e1.shareOf(e22))
        println(e1.shareOf(e21))
        println(e1.shareOf(e4) == 0.8 * 0.95)


    }
}