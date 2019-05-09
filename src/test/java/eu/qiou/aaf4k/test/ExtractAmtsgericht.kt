package eu.qiou.aaf4k.test

import java.nio.file.Files
import java.nio.file.Paths

object ExtractAmtsgericht {
    @JvmStatic
    fun main(args: Array<String>) {
        val src = "src/eu/qiou/aaf4k/plugins/Amtsgericht.kt"
        val reg = """Array\((.+)\);""".toRegex()
        val legalSymbol = """[^a-zA-Z0-9]+""".toRegex()
        val land = """\[\'([a-z]{2})\'\]""".toRegex()

        Files.readAllLines(Paths.get(src)).slice(3..65)
                .filter { it.isNotEmpty() }
                .map {
                    land.find(it)!!.groups[1]!!.value to
                            reg.find(it)!!.groups[1]!!.value.split(",")
                                    .map {
                                        it.replace("'", "")
                                                .trim()
                                                .toUpperCase()
                                                .replace("Ä", "AE")
                                                .replace("Ö", "OE")
                                                .replace("Ü", "UE")
                                                .replace("ß", "SS")
                                                .replace(")", "")
                                                .replace(legalSymbol, "_")
                                    }
                }.let {

                    it.forEachIndexed { index, pair ->
                        if (index.rem(2) == 0) {
                            // HAMBURG("Hamburg", "hh", "K1101R")
                            val id = it[index + 1].second
                            val name = it[index].second
                            val l = it[index].first

                            id.zip(name).forEach {
                                println("""${it.second}("${it.second}", "$l", "${it.first}"),""")
                            }
                        }
                    }
                }

    }
}