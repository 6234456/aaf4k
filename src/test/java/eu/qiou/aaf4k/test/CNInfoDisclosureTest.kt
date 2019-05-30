package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.CNInfoDisclosure
import eu.qiou.aaf4k.plugins.EntityInfo
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.system.measureTimeMillis

class CNInfoDisclosureTest {
    val code = "szmb000035"

    @Test
    fun management() {
        println(CNInfoDisclosure.management(code))
    }

    @Test
    fun general() {
        println(CNInfoDisclosure.generalInfo(code))
    }

    @Test
    fun fs() {
        println(CNInfoDisclosure.fs(code, 2018, 2, CNInfoDisclosure.FSType.BALANCE_STMT))
        println(CNInfoDisclosure.fs(code, 2018, 2, CNInfoDisclosure.FSType.INCOME_STMT))
        println(CNInfoDisclosure.fs(code, 2018, 2, CNInfoDisclosure.FSType.CASHFLOW_STMT))
    }

    @Test
    fun trail() = runBlocking {
        // 001001  000571
        val v = measureTimeMillis {
            CNInfoDisclosure.getEntityInfoById("0", 200).filter { it.value?.auditor?.contains("立信") ?: false }
        }



        println("load finished in $v")
/*
        v.values.forEach {
            if (it != null)
                CNInfoDisclosure.getPdfLinks(it, 2018)?.let { x ->
                    println(it.SECName)
                    async {
                        //Files.copy(URL(x).openStream(), Paths.get("data/tmp/${it.SECCode}_${it.SECName}.pdf"), StandardCopyOption.REPLACE_EXISTING)
                    }
                }
        }

        */
    }

    @Test
    fun trail1() = runBlocking {
        // 001001  000571
        val v = CNInfoDisclosure.getEntityInfoById("0", 2000)

        ObjectOutputStream(FileOutputStream("data/sec.obj")).writeObject(v)

        //println(ObjectInputStream(FileInputStream("data/sec.obj")).readObject() as Map<String, EntityInfo?>)
    }

    @Test
    fun trail2() {
        // 001001  000571

        println((ObjectInputStream(FileInputStream("data/sec.obj")).readObject() as Map<String, EntityInfo?>).map { x ->
            x.value?.let {
                CNInfoDisclosure.getPdfLinks(it, 2018)
            }
        })
    }


}