package eu.qiou.aaf4k.plugins

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import org.jsoup.Jsoup

object GermanLaw {
    var requestFactory = NetHttpTransport().createRequestFactory()

    fun clear() {
        requestFactory = null
    }

    fun law(name: String = "hgb", paragraph: String = "1", fullUrl: String = ""): Triple<String, List<String>, String?> {
        val u = "https://www.gesetze-im-internet.de/${name.toLowerCase()}/"
        val root =
                if (fullUrl.isEmpty())
                    "${u}__$paragraph.html"
                else
                    fullUrl

        with(
                requestFactory.buildGetRequest(GenericUrl(root))
        ) {
            with(Jsoup.parse(this.execute().parseAsString())) {
                return Triple(
                        this.getElementsByClass("jnenbez")[0].ownText() + " " + this.getElementsByClass("jnentitel")[0].ownText(),
                        this.getElementsByClass("jurAbsatz").map { it.ownText() },
                        with(this.getElementById("blaettern_weiter")) {
                            if (this == null || this.getElementsByTag("a").size == 0) null
                            else u + this.getElementsByTag("a")[0].attr("href")
                        }
                )
            }
        }
    }

    fun walk(name: String = "hgb"): List<Pair<String, List<String>>> {

        var (desc, content, next) = law(name)
        val res: MutableList<Pair<String, List<String>>> = mutableListOf(desc to content)

        while (true) {
            if (next == null)
                return res

            val e = law(fullUrl = next)

            val (k, v, _) = e
            next = e.third

            res.add(k to v)
        }

    }
}