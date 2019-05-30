package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.CNInfoDiscloure
import eu.qiou.aaf4k.plugins.EntityInfo
import org.junit.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class CNInfoDiscloureTest {
    val code = "szmb000035"

    @Test
    fun management() {
        println(CNInfoDiscloure.management(code))
    }

    @Test
    fun general() {
        println(CNInfoDiscloure.generalInfo(code))
    }

    @Test
    fun fs() {
        println(CNInfoDiscloure.fs(code, 2018, 2, CNInfoDiscloure.FSType.BALANCE_STMT))
        println(CNInfoDiscloure.fs(code, 2018, 2, CNInfoDiscloure.FSType.INCOME_STMT))
        println(CNInfoDiscloure.fs(code, 2018, 2, CNInfoDiscloure.FSType.CASHFLOW_STMT))
    }

    @Test
    fun trail() {
        // 001001  000571
        val v = CNInfoDiscloure.getEntityInfoById("0", 2000).filter { it.value?.auditor?.contains("立信") ?: false }

        v.values.forEach {
            if (it != null)
                CNInfoDiscloure.getPdfLinks(it, 2018)?.let { x ->
                    Files.copy(URL(x).openStream(), Paths.get("data/tmp/${it.SECCode}_${it.SECName}.pdf"), StandardCopyOption.REPLACE_EXISTING)
                }
        }
    }

    @Test
    fun trail1() {
        // 001001  000571
        val v = CNInfoDiscloure.getEntityInfoById("0", 2000)

        ObjectOutputStream(FileOutputStream("data/sec.obj")).writeObject(v)

        //println(ObjectInputStream(FileInputStream("data/sec.obj")).readObject() as Map<String, EntityInfo?>)
    }

    @Test
    fun trail2() {
        // 001001  000571

        println((ObjectInputStream(FileInputStream("data/sec0.obj")).readObject() as Map<String, EntityInfo?>).map { it.value?.SECName })
    }


}