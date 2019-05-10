package eu.qiou.aaf4k.plugins

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.time.LocalDate

object Handelsregister {
    var requestFactory = NetHttpTransport().createRequestFactory()
    private val urlPart = """'([^']+)'""".toRegex()
    private const val root = """https://www.handelsregisterbekanntmachungen.de/skripte/hrb.php?"""
    private val dateReg = """(\d{2}).(\d{2}).(\d{4})""".toRegex()

    fun clear() {
        requestFactory = null
    }

    fun get(name: String, gericht: Amtsgericht, page: Int = 0): List<String> {
        with(
                requestFactory.buildPostRequest(
                        GenericUrl("https://www.handelsregisterbekanntmachungen.de/?aktion=suche"),
                        ByteArrayContent.fromString(null, "suchart=detail&land=${gericht.land}&gericht=${gericht.id}&seite=$page&fname=${URLEncoder.encode(name, "UTF-8")}&gegenstand=0&anzv=50&order=4")
                )
        ) {
            with(Jsoup.parse(this.execute().parseAsString())) {
                return this.select("li > a[href^=javascript:]")
                        .map { root + urlPart.find(it.attr("href"))!!.groups[1]!!.value }
            }
        }
    }

    fun collect(name: String, gericht: Amtsgericht): List<String> {
        var cnt = 0
        val res: MutableList<String> = mutableListOf()
        while (true) {
            val l = get(name, gericht, cnt)

            if (l.isEmpty())
                break

            res += l

            cnt++
        }

        return res
    }

    fun walk(name: String, gericht: Amtsgericht): Map<LocalDate, String> {
        return collect(name, gericht).map { s ->
            with(requestFactory.buildGetRequest(GenericUrl(s))) {
                with(Jsoup.parse(this.execute().parseAsString())) {
                    this.select("tbody > tr > td")
                            .map { it.text().trim() }.filter { it.isNotBlank() }
                }
            }
        }.map {
            parseGermanDate(it.find { x -> dateReg.matches(x) }!!) to it.last()
        }.toMap()
    }

    private fun parseGermanDate(str: String): LocalDate {
        with(dateReg.find(str)!!.groups) {
            return LocalDate.parse("${this[3]!!.value}-${this[2]!!.value}-${this[1]!!.value}")
        }
    }

}