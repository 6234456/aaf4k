package eu.qiou.aaf4k.plugins

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object AdHocMeldung {
    private var requestFactory = NetHttpTransport().createRequestFactory()
    private val root = "https://www.dgap.de"
    private val formatter = DateTimeFormatter.ofPattern("d.MM.yy")

    private fun clear() {
        requestFactory = null
    }

    fun existsCompany(name: String): List<String>? {
        val requestData = """searchString=$name"""
        val url = GenericUrl("https://www.dgap.de/dgap/Companies/")
        val request = requestFactory!!.buildPostRequest(url, ByteArrayContent.fromString(null, requestData))

        val response = request.execute()


        // with more than one result
        if (response.statusCode == 200) {
            val html = Jsoup.parse(response.parseAsString())
            return html.select("#LSResult li a").let {
                if (it.size == 0) listOf(html.select("link[rel=\"canonical\"]").first().attr("href"))
                else it.map { x -> root + x.attr("href") }
            }
        }


        return null
    }

    fun meldung(company: String, limit: Int = 1000): List<Meldung>? {
        val urls = existsCompany(company) ?: return null

        return meldungen(urls.last(), limit)
    }

    private fun meldungen(companyURL: String, limit: Int = 1000): List<Meldung> {
        val searchString = "&id=1020&page=1&limit=$limit"

        val html = Jsoup.parse(requestFactory.buildGetRequest(GenericUrl(companyURL + searchString)).execute().parseAsString())
        val company = html.select(".logo img").first().attr("alt")

        val res = html.select("tbody tr").map {
            val link = root + it.select("a").first().attr("href")
            val category = it.select(".content_type img").first().attr("alt")
            val title = it.select(".content_text strong").first().ownText().trim()//content_text
            GlobalScope.async {
                val src = Jsoup.parse(requestFactory.buildGetRequest(GenericUrl(link)).execute().parseAsString())

                val title2 = try {
                    src.select(".news_header").first().ownText().trim()
                } catch (e: NullPointerException) {
                    ""
                }
                val content = try {
                    src.select(".left_col").first().text().trim()
                } catch (e: NullPointerException) {
                    src.body().text().trim()
                }// news_main

                val date = LocalDate.parse(title.take(8), formatter)  // news_top_date
                Meldung(title, title2, content, company, date, link, category)
            }
        }

        return runBlocking {
            res.map {
                it.await().apply {
                    println("$date $title2")
                }
            }
        }
    }

}

data class Meldung(val title: String, val title2: String, val content: String, val company: String, val date: LocalDate, val URL: String, val category: String)