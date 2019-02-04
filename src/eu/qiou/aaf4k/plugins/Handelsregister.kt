package eu.qiou.aaf4k.plugins

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.time.LocalDate

object Handelsregister {
    var requestFactory = NetHttpTransport().createRequestFactory()
    val urlPart = """'([^']+)'""".toRegex()
    val root = """https://www.handelsregisterbekanntmachungen.de/skripte/hrb.php?"""

    fun clear() {
        requestFactory = null
    }

    fun get(name: String, gericht: Amtsgericht, page: Int = 0): List<String> {
        with(
                requestFactory.buildPostRequest(
                        GenericUrl("https://www.handelsregisterbekanntmachungen.de/?aktion=suche"),
                        ByteArrayContent.fromString(null, "suchart=detail&land=${gericht.land}&gericht=${gericht.id}&seite=${page}&fname=${URLEncoder.encode(name, "UTF-8")}&gegenstand=0&anzv=50&order=4")
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
        var res: List<String> = listOf()
        while (true) {
            val l = get(name, gericht, cnt)

            if (l.size == 0)
                break

            res += l

            cnt++
        }

        return res
    }

    fun walk(name: String, gericht: Amtsgericht): Map<Int, List<String>> {
        var cnt = 0
        return collect(name, gericht).map {
            cnt++ to with(requestFactory.buildGetRequest(GenericUrl(it))) {
                with(Jsoup.parse(this.execute().parseAsString())) {
                    this.select("tbody > tr > td")
                            .map { it.text() }.filter { it.isNotBlank() }
                }
            }
        }.toMap()
    }

}