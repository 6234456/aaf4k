package eu.qiou.aaf4k.plugins

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import eu.qiou.aaf4k.util.strings.recode
import eu.qiou.aaf4k.util.time.TimeSpan
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.jsoup.Jsoup
import java.text.NumberFormat

object CNInfoDisclosure {
    var requestFactory = NetHttpTransport().createRequestFactory()
    var year: Int = 2018  // year for the annual financial statments

    fun clear() {
        requestFactory = null
    }

    // http://www.cninfo.com.cn/information/management/szmb000002.html
    fun management(code: String): List<List<String>> {
        with(
                requestFactory.buildGetRequest(GenericUrl("http://www.cninfo.com.cn/information/management/$code.html"))
        ) {
            with(Jsoup.parse(this.execute().parseAsString())) {
                return this.getElementsByClass("zx_data3").map { it.ownText().recode("ISO-8859-1", "GB2312") }.chunked(4).zip(
                        this.getElementsByClass("zx_data2").map { it.ownText().recode("ISO-8859-1", "GB2312") }
                ).map { it.first + it.second }
            }
        }
    }

    fun generalInfo(code: String): Map<String, String> {
        with(
                requestFactory.buildGetRequest(GenericUrl("http://www.cninfo.com.cn/information/brief/${code}.html"))
        ) {
            with(Jsoup.parse(this.execute().parseAsString())) {
                return this.getElementsByClass("zx_data").map { it.ownText().recode("ISO-8859-1", "GB2312") }.zip(
                        this.getElementsByClass("zx_data2").map { it.ownText().recode("ISO-8859-1", "GB2312") }
                ).toMap()
            }
        }
    }

    suspend fun getEntityInfoById(query: String, cnt: Int = 60, filter: (String) -> Boolean = { true }): Map<String, EntityInfo?> {
        val requestData = """keyWord=$query&maxNum=$cnt"""
        val url = GenericUrl("http://www.cninfo.com.cn/new/information/topSearch/query")
        val request = requestFactory!!.buildPostRequest(url, ByteArrayContent.fromString(null, requestData))

        with(request.headers) {
            this.accept = "application/json"
            this.userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"
            this.set("Referer", "http://www.szse.cn/disclosure/listed/fixed/index.html")
        }

        with(JSONParser().parse(request.execute().parseAsString()) as JSONArray) {
            with(this.filter {
                it as JSONObject
                it["category"] == "A股" || it["category"] == "股份报价"
            }) {
                if (this.isEmpty()) return mapOf()

                val res = this.map {
                    it as JSONObject
                    val v = it["code"]!!.toString()
                    val org = it["orgId"]!!.toString()
                    val cate = it["category"]!!.toString()
                    GlobalScope.async {
                        if (filter(v)) {
                            if (cate == "股份报价") {
                                // http://www.cninfo.com.cn/new/newInterface/marketOverview
                                with(JSONParser().parse(requestFactory!!.buildGetRequest(GenericUrl("http://www.cninfo.com.cn/new/newInterface/marketOverview?secCode=$v&orgId=$org&secType=notb"))
                                        .execute().parseAsString()) as JSONObject) {

                                    EntityInfo(v, org, it["zwjc"].toString(), "", "", this["secName"].toString(), "",
                                            this["address"].toString(), "", "", "", "",
                                            0.0, "",
                                            "", false,
                                            getPdfLinks(v, org, false, year, isQuotation = true) ?: "")
                                }
                            } else get(v)
                        } else null
                    }
                }

                return runBlocking {
                    res.map {
                        it.await().apply {
                            if (this != null)
                                println("$SECCode $SECName $fs")
                        }
                    }.filter {
                        it != null
                    }.map {
                        it!!.SECCode to it
                    }.toMap()
                }
            }
        }
    }

    private fun isSZ(code: String, orgId: String): Boolean {
        //http://www.cninfo.com.cn/new/disclosure/stock?orgId=$orgId&stockCode=$code
        //<script type="text/javascript">
        //var stockCode = "600100";
        //var orgId = "gssh0600100";
        //var plate = "sse"
        //</script>
        val url = GenericUrl("http://www.cninfo.com.cn/new/disclosure/stock?orgId=$orgId&stockCode=$code")
        val request = requestFactory!!.buildGetRequest(url)

        return !Jsoup.parse(request.execute().parseAsString()).select("script").map { it.data() }.any { it.contains("\"sse\"") }
    }

    private fun get(code: String): EntityInfo? {
        val requestData = """stockCode=$code"""
        val url = GenericUrl("http://www.cninfo.com.cn/data/cube/profile")
        val request = requestFactory!!.buildPostRequest(url, ByteArrayContent.fromString(null, requestData))

        with(request.headers) {
            this.userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"
            this.set("Referer", "http://www.szse.cn/disclosure/listed/fixed/index.html")
        }

        with(try {
            JSONParser().parse(request.execute().parseAsString()) as JSONArray
        } catch (e: Exception) {
            null
        }) {
            if (this == null || this.isEmpty()) return null

            return this[0]!!.let {
                it as JSONObject

                val id = it["orgid"].toString()
                val sz = isSZ(code, id)
                val pdf = getPdfLinks(code, id, sz, year)

                EntityInfo(code, id, it["orgname"].toString(), it["f030v"].toString(),
                        it["f034v"].toString(), it["orgname"].toString(), it["f001v"].toString(),
                        it["f004v"].toString(), it["f011v"].toString(), it["f012v"].toString(),
                        it["f022v"].toString(),
                        it["f021v"].toString(),
                        try {
                            NumberFormat.getInstance().parse(it["f010d"].toString()).toDouble()
                        } catch (e: Exception) {
                            0.0
                        },
                        it["f018v"].toString(),
                        it["f039v"].toString(), sz, pdf ?: ""
                )
            }
        }
    }


    fun getPdfLinks(SECCode: String, orgCode: String, sz: Boolean, year: Int, pageSize: Int = 30, contentSearch: String = "", isQuotation: Boolean = false): String? {
        val requestData = if (isQuotation) """pageNum=1&pageSize=$pageSize&plate=&tabName=fulltext&column=neeq&stock=${SECCode},${orgCode}&searchkey=$contentSearch&secid=&category=category_dqgg_gfzr&seDate=${year}-01-01 ~ ${year + 1}-12-31"""
        else """pageNum=1&pageSize=$pageSize&plate=${if (sz) "sz" else "shmb"}&tabName=fulltext&column=szse&stock=${SECCode},${orgCode}&searchkey=$contentSearch&secid=&category=category_ndbg_szsh&seDate=${year}-01-01 ~ ${year + 1}-12-31"""
        val url = GenericUrl("http://www.cninfo.com.cn/new/hisAnnouncement/query")
        val request = requestFactory!!.buildPostRequest(url, ByteArrayContent.fromString(null, requestData))

        return ((JSONParser().parse(request.execute().parseAsString()) as JSONObject)["announcements"]!! as JSONArray).filter {
            it as JSONObject
            val x = it.get("announcementTitle")!!.toString()
            !(x.contains("英文版") || x.contains("摘要") || x.contains("已取消")) && x.contains(year.toString())
        }.let {
            if (it.isEmpty()) null
            else "http://static.cninfo.com.cn/" + (it[0] as JSONObject)["adjunctUrl"].toString()
        }

    }



    enum class FSType(val typeName: String) {
        INCOME_STMT("incomestatements"),
        BALANCE_STMT("balancesheet"),
        CASHFLOW_STMT("cashflow"),
    }

    fun fs(code: String, year: Int, quarter: Int, type: FSType): List<List<String>> {

        val d = TimeSpan.forQuarter(year, quarter).end.toString()
        val yyyy = year.toString()
        val mm = d.replace(yyyy, "")

        val reg = Regex("""\D""")
        with(
                requestFactory
                        .buildPostRequest(
                                GenericUrl("http://www.cninfo.com.cn/information/stock/${type.typeName}_.jsp?stockCode=${reg.replace(code, "")}"),
                                ByteArrayContent.fromString(null, "yyyy=$yyyy&mm=$mm&cwzb=${type.typeName}")
                        )
        ) {
            this.headers.contentType = "application/x-www-form-urlencoded"

            with(Jsoup.parse(this.execute().parseAsString())) {
                if (this.getElementsByClass("zx_left").count() == 0) {
                    //the format for the latest period
                    // http://www.cninfo.com.cn/information/balancesheet/szmb000011.html
                    val codec = Regex("""/(\w+?\.html)(?:["']?;?)$""").find(this.getElementsByTag("script")[0].html())!!.groupValues[1]

                    with(Jsoup.parse(
                            requestFactory.buildGetRequest(GenericUrl("http://www.cninfo.com.cn/information/${type.typeName}/$codec"))
                                    .execute().parseAsString()
                    )
                            .getElementsByClass("zx_left")[0].getElementsByTag("td")
                            .map { it.text().recode("ISO-8859-1", "GB2312") }
                            .chunked(2))
                    {
                        return this.filterIndexed { i, _ -> i % 2 == 0 } + this.filterIndexed { i, _ -> i % 2 == 1 }
                    }
                } else {
                    with(this.getElementsByClass("zx_left")[0].getElementsByTag("td")
                            .map { it.text() }
                            .chunked(2))
                    {
                        return this.filterIndexed { i, _ -> i % 2 == 0 } + this.filterIndexed { i, _ -> i % 2 == 1 }
                    }
                }
            }
        }
    }
}