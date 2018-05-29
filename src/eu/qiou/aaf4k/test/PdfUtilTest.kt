package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.io.PdfUtil
import eu.qiou.aaf4k.util.template.Template
import org.junit.Test
import java.awt.Rectangle

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
                PdfUtil.readFile("src/eu/qiou/aaf4k/test/credentials.SKR 04 2017.pdf"),
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

        val map = l.mapIndexed { index: Int, s: String ->
            index.toString() to listOf(s)
        }.toMap()

        ExcelUtil.writeData("skr4.xls", data = map)

        val regexFirstUpper = Regex("""^\s*(?:([A-ZÄÜ]\s+)*)(\d{4,})""")

        Template(listOf(
                //   Template.HeadingFormat("KontoNr", formatData = ExcelUtil.DataFormat.INT.format),
                Template.HeadingFormat("KontoName", formatData = ExcelUtil.DataFormat.STRING.format)
        ),
                map.values.toList()

        ).build("skr4.xls")
    }
}