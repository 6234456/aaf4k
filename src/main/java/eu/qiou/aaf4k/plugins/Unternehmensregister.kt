package eu.qiou.aaf4k.plugins

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import org.jsoup.Jsoup

//search for the company Amtsgericht
object Unternehmensregister {
    var requestFactory = NetHttpTransport().createRequestFactory()
    val root = "https://www.unternehmensregister.de"
    private val urlPart = """'([^']+)'""".toRegex()
    private val dateReg = """(\d{2}).(\d{2}).(\d{4})""".toRegex()

    fun clear() {
        requestFactory = null
    }

    fun search(company: String): String {
        // get jsession which is stored in the action of the #globalSearchForm
        val starter = Jsoup.parse(requestFactory.buildGetRequest(GenericUrl(root))
                .execute().parseAsString())
        val jsession = starter.select("form#globalSearchForm")
                .first().attr("action")
        val sessionId = jsession.split(";")[1]
        val param = starter.getElementById("j_id1:javax.faces.ViewState:1").attr("value")

        val f = requestFactory.buildPostRequest(GenericUrl("$root$jsession"),
                ByteArrayContent.fromString(null, "globalSearchForm=globalSearchForm&globalSearchForm%3AextendedResearchCompanyName=$company&globalSearchForm%3Ahidden_element_used_for_validation_only=aaa&submitaction=globalsearch&globalsearch=true&globalSearchForm%3AbtnExecuteSearchOld=Suchen&javax.faces.ViewState=$param"))

        val param2 = Jsoup.parse(f.execute().parseAsString()).getElementById("j_id1:javax.faces.ViewState:2").attr("value")

        // j_id1:javax.faces.ViewState:2
        // $root/ureg/result.html;$sessionId
        // hppForm=hppForm&hppForm%3Ahitsperpage=10000&submitaction=hpp&javax.faces.ViewState=$param2

        var cnt = 0
        fun fetchData(page: Int): List<String> {
            return Jsoup.parse(requestFactory.buildPostRequest(GenericUrl("$root/ureg/result.html;$sessionId?submitaction=pathnav&page.$page=page"),
                    ByteArrayContent.fromString(null, "hppForm=hppForm&hppForm%3Ahitsperpage=100&submitaction=hpp&javax.faces.ViewState=$param2"))
                    .execute().parseAsString()).let {
                if (page == 1) {
                    cnt = "\\d+".toRegex().find(it.select(".page_count").first().text())!!.value.toInt()
                }
                it.select("div.company_result").map { x -> x.text() }
            }
        }

        val res = fetchData(1)

        (2..cnt).fold(res) { acc, i ->
            acc + fetchData(i)
        }.run {
            println(size)
            this.forEach {
                println(it)
            }
        }

        return ""
    }
}