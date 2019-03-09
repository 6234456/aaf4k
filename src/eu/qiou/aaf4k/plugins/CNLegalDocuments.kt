package eu.qiou.aaf4k.plugins

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import eu.qiou.aaf4k.util.io.JSExecutor
import eu.qiou.aaf4k.util.mkString
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.util.*

/**
 * https://github.com/sml2h3/mmewmd_crack_for_wenshu/blob/master/spider_demo.py
 *
 * implemented in kotlin
 */

object CNLegalDocuments {
    var requestFactory = NetHttpTransport().createRequestFactory()
    private val s1_key = "FSSBBIl1UgzbN7N80S"
    private val t1_key = "FSSBBIl1UgzbN7N80T"
    private val s1 = "$s1_key=([._a-zA-Z0-9]+)".toRegex()
    private val t1 = "$t1_key=([._a-zA-Z0-9]+)".toRegex()

    val vjkl5r = "vjkl5=([._a-zA-Z0-9]+)".toRegex()

    /*
    private val t2_key = "FSSBBIl1UgzbN7N443T"
    private val s2_key = "FSSBBIl1UgzbN7N443T"
    private val s2 = "$s2_key=([._a-zA-Z0-9]+)".toRegex()
    private val t2 = "$t2_key=([._a-zA-Z0-9]+)".toRegex()
*/
    fun clear() {
        requestFactory = null
    }

    private fun guid(): String {
        val a = UUID.randomUUID().toString()
        return a.take(18) + a.drop(19)
    }

    private fun urlGen(keywords: List<String>, guid: String? = null): String {
        val keyword = toKeyWord(keywords)
        return "http://wenshu.court.gov.cn/list/list/?sorttype=1&number=&guid=${guid
                ?: guid()}&conditions=searchWord+QWJS+++${URLEncoder.encode("全文检索", "UTF-8")}:$keyword"
    }

    private fun toKeyWord(keywords: List<String>): String {
        return keywords.map { URLEncoder.encode(it, "UTF-8") }.mkString("%20", "", "")
    }

    fun search(keywords: List<String>, index: Int = 1) {

        val guid = guid()

        var ywtu: String
        var f80t: String
        var f80s: String
        var f80t_n: String

        var vjkl5: String
        var vl5x: String
/*
        var f443t: String
        var f443s: String
        var f443t_n: String
*/
        var meta: String

        val url = urlGen(keywords, guid)
        val b = requestFactory.buildGetRequest(GenericUrl(url))

        println(url)

        b.execute().apply {
            val h = headers["Set-Cookie"].toString()

            f80s = s1.find(h)!!.groups[1]!!.value
            f80t = t1.find(h)!!.groups[1]!!.value
/*
            f443s = s2.find(h)!!.groups[1]!!.value
            f443t = t2.find(h)!!.groups[1]!!.value
*/
            Jsoup.parse(this.parseAsString()).let {
                meta = it.getElementById("9DhefwqGPrzGxEp9hPaoag").attr("content")
                ywtu = JSExecutor("script/ywtu.js").invoke("getc", meta).toString()
            }

            //encrypt.js
            f80t_n = JSExecutor("script/encrypt.js").invoke("getCookies", meta, f80t, ywtu).toString()
            // f443t_n = JSExecutor("script/encrypt.js").invoke("getCookies", meta, f443t, ywtu).toString()
        }

        b.apply {
            headers.cookie = "FSSBBIl1UgzbN7Nenable=true; $s1_key=$f80s; $t1_key=$f80t_n"

            execute().apply {
                val h = headers["Set-Cookie"].toString()
                println(h)
                vjkl5 = vjkl5r.find(h)!!.groups[1]!!.value
                vl5x = JSExecutor("script/vl5x.js").invoke("getKey", vjkl5).toString()
            }
        }

        val u = "http://wenshu.court.gov.cn/List/ListContent"
        val requestData = "Param=${URLEncoder.encode("全文检索", "UTF-8")}:${toKeyWord(keywords)}&Index=$index&Page=10&Order=${URLEncoder.encode("法院层级", "UTF-8")}&Direction=asc&vl5x=$vl5x&number=&guid=$guid"

        println(requestData)
        val post = requestFactory.buildPostRequest(GenericUrl(u), ByteArrayContent.fromString(null, requestData))

        post.apply {
            headers.cookie = "FSSBBIl1UgzbN7Nenable=true; $s1_key=$f80s; $t1_key=$f80t_n; vjkl5=$vjkl5"

            println(execute().parseAsString())
        }
/*


        with(
                )
        ) {
            println(this.execute().parseAsString())

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
        */
    }
}