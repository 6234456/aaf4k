package eu.qiou.aaf4k.plugins

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import eu.qiou.aaf4k.util.io.JSONUtil
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser


/**
 *  fetch and parse the financial statements listed in the shengzhen stock exchange
 *  http://www.szse.cn/disclosure/listed/fixed/index.html
 */
object SZSEReportings {
    var requestFactory = NetHttpTransport().createRequestFactory()

    fun clear() {
        requestFactory = null
    }

    /**
     * @param index the stock-index of the listed company
     */
    private fun get(index: String): JSONObject {
        val requestData = """ {"seDate":["",""],"stock":["${index}"],"channelCode":["fixed_disc"],"pageSize":30,"pageNum":1} """
        val url = GenericUrl("http://www.szse.cn/api/disc/announcement/annList?random=${java.util.Random().nextFloat()}")
        val request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString("application/json", requestData))

        with(request.headers) {
            this.accept = "application/json"
            this.userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"
            this.set("Referer", "http://www.szse.cn/disclosure/listed/fixed/index.html")
        }

        return JSONParser().parse(request.execute().parseAsString()) as JSONObject
    }

    private fun get(index: Int): JSONObject {
        return get(String.format("%06d", index))
    }

    private fun getPdfLinks(obj: JSONObject): Map<String, String> {
        return JSONUtil.query<JSONArray>(obj, "data").map { v ->
            (v as JSONObject).let {
                it.get("title").toString() to "http://disc.static.szse.cn/download" + it.get("attachPath").toString()
            }
        }.toMap()
    }

    fun getPdfLinks(index: String): Map<String, String> {
        return getPdfLinks(get(index))
    }

    /**
     * @return map of title to the pdf-url
     */
    fun getPdfLinks(index: Int): Map<String, String> {
        return getPdfLinks(get(index))
    }


    fun getEntityInfoById(query: String, cnt: Int = 60): Map<String, EntityInfo> {

        val requestData = """ticker=${query}&limit=${cnt}&date=365"""
        val url = GenericUrl("http://xbrl.cninfo.com.cn/do/stockreserch/getcompanybyprefix")
        val request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString(null, requestData))

        request.headers.contentType = "application/x-www-form-urlencoded"
        return request.execute().parseAsString().split(",").map {
            val t = it.split("#")
            t.get(0) to EntityInfo(t.get(0), t.get(1), t.get(3), t.get(4), t.get(2))
        }.toMap()
    }

    data class EntityInfo(val SECCode: String, val SECName: String, val industry1: String, val industry2: String, val orgName: String)
}