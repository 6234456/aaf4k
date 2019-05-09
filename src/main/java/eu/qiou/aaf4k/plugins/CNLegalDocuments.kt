package eu.qiou.aaf4k.plugins

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
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

    private fun urlGen(keywords: List<String>, guid: String? = null, MmEwMD: String? = null): String {
        val keyword = toKeyWord(keywords)
        //return "http://wenshu.court.gov.cn/list/list/?${if (MmEwMD == null) "" else "MmEwMD=${MmEwMD}&"}sorttype=1&guid=${guid ?: guid()}&conditions=searchWord+QWJS+++${URLEncoder.encode("全文检索", "UTF-8")}:$keyword"
        return "http://wenshu.court.gov.cn/list/list/?${if (MmEwMD == null) "" else "MmEwMD=${MmEwMD}&"}sorttype=1&conditions=searchWord+QWJS+++${URLEncoder.encode("全文检索", "UTF-8")}:$keyword"
    }

    private fun toKeyWord(keywords: List<String>): String {
        return keywords.map { URLEncoder.encode(it, "UTF-8") }.mkString("%20", "", "")
    }

    private fun headersTraits(headers: HttpHeaders) {
        headers.accept = "*/*"
        headers.acceptEncoding = "gzip, deflate"
        headers.set("Accept-Language", "zh-CN,zh;q=0.9")
        headers.set("Connection", "keep-alive")
        headers.userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36"
        headers.contentType = "application/x-www-form-urlencoded; charset=UTF-8"
        headers.set("Host", "wenshu.court.gov.cn")
        headers.set("Origin", "http://wenshu.court.gov.cn")
    }

    fun search(keywords: List<String>, index: Int = 1) {

        val guid = guid()

        var ywtu: String = ""
        var f80t: String = ""
        var f80s: String = ""
        var f80t_n: String = ""

        var vjkl5: String = ""
        var vl5x: String = ""

        var meta: String = ""

        var isModel2 = false

        val url = urlGen(keywords, guid)
        val b = requestFactory.buildGetRequest(GenericUrl(url))
        headersTraits(b.headers)
        println(url)

        b.execute().apply {
            val h = headers["Set-Cookie"].toString()
            println(h)

            if (vjkl5r.containsMatchIn(h)) {
                vjkl5 = vjkl5r.find(h)!!.groups[1]!!.value
                vl5x = JSExecutor("script/vl5x.js").invoke("getKey", vjkl5).toString()

                isModel2 = true
            } else {
                f80s = s1.find(h)!!.groups[1]!!.value
                f80t = t1.find(h)!!.groups[1]!!.value

                Jsoup.parse(this.parseAsString()).let {
                    meta = it.getElementById("9DhefwqGPrzGxEp9hPaoag").attr("content")
                    ywtu = JSExecutor("script/ywtu.js").invoke("getc", meta).toString()
                }

                f80t_n = JSExecutor("script/encrypt.js").invoke("getCookies", meta, f80t, ywtu).toString()
            }
        }

        b.apply {
            if (!isModel2) {
                headers.cookie = "FSSBBIl1UgzbN7Nenable=true; $s1_key=$f80s; $t1_key=$f80t_n; wzwsvtime=${java.time.Instant.now().epochSecond}; wzwschallenge=-1; wzwstemplate=NQ==; wzwsconfirm=0e561c10c60c2f0d44410644eb3c2403; _gscbrs_2116842793=1;  _gscs_2116842793=47659453ttzz3o20|pv:14; _gscu_2116842793=47626758817stt18; ccpassport=1ff98c661b8f424096c234ce889da9b0; "
                headersTraits(headers)

                execute().apply {
                    val h = headers["Set-Cookie"].toString()
                    println(h)
                    vjkl5 = vjkl5r.find(h)!!.groups[1]!!.value
                    vl5x = JSExecutor("script/vl5x.js").invoke("getKey", vjkl5).toString()

                    Jsoup.parse(this.parseAsString()).let {
                        meta = it.getElementById("9DhefwqGPrzGxEp9hPaoag").attr("content")
                        // ywtu = JSExecutor("script/ywtu.js").invoke("getc", meta).toString()
                    }

                    f80t_n = JSExecutor("script/encrypt.js").invoke("getCookies", meta, f80t_n, ywtu).toString()
                }
            }
        }

        val u = "http://wenshu.court.gov.cn/List/ListContent"
        val requestData = "Param=${URLEncoder.encode("全文检索", "UTF-8")}:${toKeyWord(keywords)}&Index=$index&Page=10&Order=${URLEncoder.encode("法院层级", "UTF-8")}&Direction=asc&vl5x=$vl5x&guid=$guid&number=wens"

        println(requestData)
        val post = requestFactory.buildPostRequest(GenericUrl(u), ByteArrayContent.fromString(null, requestData))

        post.apply {
            //  headers.cookie = "FSSBBIl1UgzbN7Nenable=true; $s1_key=$f80s; $t1_key=$f80t_n; vjkl5=$vjkl5; wzwsvtime=${java.time.Instant.now().epochSecond}; wzwschallenge=-1; wzwstemplate=NQ==; wzwsconfirm=0e561c10c60c2f0d44410644eb3c2403; _gscbrs_2116842793=1;  _gscs_2116842793=47659453ttzz3o20|pv:14; _gscu_2116842793=47626758817stt18; ccpassport=1ff98c661b8f424096c234ce889da9b0;"
            headersTraits(headers)

            execute().apply {
                println(headers["Set-Cookie"].toString())
                println(parseAsString())
            }
        }
    }
}