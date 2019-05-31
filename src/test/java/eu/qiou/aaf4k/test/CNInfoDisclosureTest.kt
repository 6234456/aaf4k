package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.plugins.CNInfoDisclosure
import eu.qiou.aaf4k.plugins.EntityInfo
import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.template.Template
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.junit.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList
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
            CNInfoDisclosure.getEntityInfoById("1", 200000) { !it.contains("0") }.filter {
                it.value?.auditor?.contains("立信") ?: false
            }
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
        val j = measureTimeMillis {
            val v = CNInfoDisclosure.getEntityInfoById("0", 20000)
            ObjectOutputStream(FileOutputStream("data/sec.obj")).writeObject(v)
        }

        println(j)
    }

    @Test
    fun pdf() = runBlocking {
        val v = CNInfoDisclosure.getEntityInfoById("839678").values.elementAt(0)!!
        println(v)
    }

    // download with multi-thread
    @Test
    fun pdf0() = runBlocking {
        val v = (ObjectInputStream(FileInputStream("data/sec.obj")).readObject() as Map<String, EntityInfo>).filter {
            it.value.auditor.contains("立信")
        }

        println("totally: ${v.size}")

        v.map {
            if (it.value.SECCode > "300642")
                it.value.downloadFS()
            else
                null
        }.forEach {
            it?.await()?.let { x ->
                println("${x.SECCode} ${x.SECName}")
            }
        }
    }

    // search page for the info
    @Test
    fun pdf1() = runBlocking {
        searchPage().map {
            it.await().apply {
                println(this)
            }
        }.toMap().filterValues {
            it
        }.forEach {
            println(it)
        }
    }

    fun searchPage(): List<Deferred<Pair<String, Boolean>>> {
        return Files.walk(Paths.get("data/tmp")).toList()
                .map { x ->
                    GlobalScope.async {
                        if (x.toFile().isFile)
                            PDDocument.load(x.toFile()).let {
                                val v = PDFTextStripper().getText(it).contains("德国")
                                it.close()

                                x.toFile().nameWithoutExtension to v
                            }
                        else "" to false
                    }
                }
    }

    @Test
    fun sum() {
        val v = (ObjectInputStream(FileInputStream("data/sec.obj")).readObject() as Map<String, EntityInfo>).filter {
            it.value.auditor.contains("立信")
        }

        val d = Files.readAllLines(Paths.get("data/tmp/res")).map {
            v[it.take(6)]
        }.map {
            mapOf(
                    "id" to it!!.orgCode,
                    "name" to it.orgName,
                    "nameEN" to it.orgNameEN,
                    "fs" to it.fs
            )
        }

        Template(listOf(
                Template.HeadingFormat(value = "id", formatData = ExcelUtil.DataFormat.STRING.format),
                Template.HeadingFormat(value = "name", formatData = ExcelUtil.DataFormat.STRING.format, columnWidth = 48),
                Template.HeadingFormat(value = "nameEN", formatData = ExcelUtil.DataFormat.STRING.format),
                Template.HeadingFormat(value = "fs", formatData = ExcelUtil.DataFormat.STRING.format)
        ), d).build("data/trail10.xlsx")
    }


}