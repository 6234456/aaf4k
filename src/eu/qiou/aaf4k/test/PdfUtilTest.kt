package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.io.PdfUtil
import eu.qiou.aaf4k.util.template.Template
import eu.qiou.aaf4k.util.toIndexedMap
import org.junit.Test
import java.awt.Rectangle
import java.io.File

class PdfUtilTest {

    @Test
    fun extractText() {
        val f:(String) -> String = {
            val regex = Regex("""^\s*(\d{4,}|([A-ZÄÜ\s/]+)\s\d{4,})""")
            val regexDash = Regex("""-$""")
            val regexFirstUpper = Regex("""^\s*[A-ZÄÜ]""")
            val regexUnd = Regex("""^\s*und\s""")
            val regexFootnote = Regex("""\d+\)\s*$""")
            val regexDoubleBracket = Regex("""\([^)]+\)""")
            val lines = it.split("\n")

            lines.foldIndexed(""){
                i, r, e ->
                    val tmpE = e.trim()
                    var dash = when{
                        regex.containsMatchIn(e) -> {
                                                        r.trim()
                                                        "\n" + tmpE
                                                    }
                        tmpE.isEmpty() -> ""
                        else -> tmpE
                    }

                    val nextLine = if(i < lines.count() -1) lines[i+1].trim() else ""

                    // ends with dash "-" but next line neither start with capital letter nor with "und"
                    if(regexDash.containsMatchIn(dash)){
                        if( !regexFirstUpper.containsMatchIn(nextLine) && ! regexUnd.containsMatchIn(nextLine)){
                            dash = dash.dropLast(1)
                        }
                    }else{
                        if(dash.isEmpty() && ! nextLine.isEmpty()){
                            dash = "\n"
                        }else{
                            dash += " "
                        }
                    }

                    while (regexFootnote.containsMatchIn(dash) && !regexDoubleBracket.containsMatchIn(dash)){
                        dash = regexFootnote.replace(dash,"")
                    }

                    r+dash


            }
        }

        val a1 = PdfUtil.extractText(
                PdfUtil.readFile("data/de/SKR-04.pdf"),
                pageFilter = {_ ,_-> true},
                regions = mutableMapOf(
                        "a" to Rectangle(130,50,690,750)
                )
                , stringProcessor = f
        )

        val l = mutableListOf<String>()
        a1.forEach {
            it.forEach(
                    {
                        _, s ->
                            l.addAll(s.split("\n"))
                    }
            )
        }

        val reg1 = """^[A-Z\s]{0,5}(\d{4})\s(\S.+)""".toRegex()
        val reg2 = """[a-zA-Z]""".toRegex()
        val reg3 = """^\s*-\d{2}\s*""".toRegex()
        val reg4 = """^\s*–\s*Restlaufzeit""".toRegex()

        val fs: (String) -> String = {
            val e = reg1.find(it)!!.groups
            reg3.replace(e[2]!!.value, "")
        }

        val map1 = l.filter { it.isNotBlank() && reg1.containsMatchIn(it) && reg2.containsMatchIn(reg1.find(it)!!.groups[2]!!.value) }

        val map = map1.mapIndexed { index: Int, s: String ->
            val e = reg1.find(s)!!.groups
            var f = fs(s)
            if (reg4.containsMatchIn(f)) {
                var i = index - 1
                if (e[1]!!.value == "1335") {
                    println("$i ${map1[i]}")
                    println("${i - 1} ${map1[i - 1]}")
                }
                while (reg4.containsMatchIn(fs(map1[i]))) {
                    i--
                }

                if (e[1]!!.value == "1335") {
                    println("${i} ${map1[i]}")
                    println("${fs(map1[i])}")
                }

                f = fs(map1[i]).split("–")[0].trim() + " " + f
            }
            index.toString() to listOf(e[1]!!.value + "#" + f)
        }.toMap()



        ExcelUtil.writeData("data/de/skr4.xls", data = map)

        val regexFirstUpper = Regex("""^\s*(?:([A-ZÄÜ]\s+)*)(\d{4,})""")

        Template(listOf(
                //   Template.HeadingFormat("KontoNr", formatData = ExcelUtil.DataFormat.INT.format),
                Template.HeadingFormat("KontoName", formatData = ExcelUtil.DataFormat.STRING.format)
        ),
                map.values.map { it.toIndexedMap() }

        ).build("skr4.xls")
    }


    @Test
    fun extractChinaACC() {
        println(PdfUtil.mining(File("tmp/acc.pdf"),
                { i, _ -> i >= 2 },
                mapOf(
                        "a" to Rectangle(50, 50, 300, 750)
                )
        ))
    }
}