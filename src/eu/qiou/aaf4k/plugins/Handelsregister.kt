package eu.qiou.aaf4k.plugins

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import org.jsoup.Jsoup
import java.net.URLEncoder

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
}