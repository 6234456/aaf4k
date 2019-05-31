package eu.qiou.aaf4k.plugins

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.javanet.NetHttpTransport
import eu.qiou.aaf4k.util.io.JSONUtil
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.text.NumberFormat


/**
 *  fetch and parse the financial statements listed in the shengzhen stock exchange
 *  http://www.szse.cn/disclosure/listed/fixed/index.html
 */
object SZSEDiscloure {
    private var requestFactory: HttpRequestFactory? = NetHttpTransport().createRequestFactory()

    fun clear() {
        requestFactory = null
    }

    /**
     * @param index the stock-index of the listed company
     */
    private fun get(index: String): JSONObject {
        val requestData = """ {"seDate":["",""],"stock":["$index"],"channelCode":["fixed_disc"],"pageSize":30,"pageNum":1} """
        val url = GenericUrl("http://www.szse.cn/api/disc/announcement/annList?random=${java.util.Random().nextFloat()}")
        val request = requestFactory!!.buildPostRequest(url, ByteArrayContent.fromString("application/json", requestData))

        with(request.headers) {
            this.accept = "application/json"
            this.userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"
            this.set("Referer", "http://www.szse.cn/disclosure/listed/fixed/index.html")
        }

        return JSONParser().parse(request.execute().parseAsString()) as JSONObject
    }

    fun get(index: Int): JSONObject {
        return get(indexToCode(index))
    }

    private fun indexToCode(index: Int) = String.format("%06d", index)

    private fun getPdfLinks(obj: JSONObject): Map<String, String> {
        return JSONUtil.query<JSONArray>(obj, "data").map { v ->
            (v as JSONObject).let {
                it["title"].toString() to "http://disc.static.szse.cn/download" + it["attachPath"].toString()
            }
        }.toMap()
    }

    fun getPdfLinks(index: String, year: Int? = null, quarter: Int? = null, ignoreOutdated: Boolean = false, isAbstract: Boolean = false): Map<String, String> {
        var res = getPdfLinks(get(index))

        year?.let {
            val regYear = "$it".toRegex()
            res = res.filterKeys { i -> regYear.containsMatchIn(i) }
        }

        quarter?.let {
            val regQuarter = when (it) {
                1 -> "第一季度"
                2 -> "半年度"
                3 -> "第三季度"
                4 -> "年度"
                else -> throw UnsupportedOperationException()
            }.toRegex()

            res = res.filterKeys { it1 -> regQuarter.containsMatchIn(it1) }

            if (quarter == 4) {
                val regTmp = "半年度".toRegex()
                res = res.filterKeys { it1 -> !regTmp.containsMatchIn(it1) }
            }
        }

        if (ignoreOutdated) {
            val regTmp = "已取消".toRegex()
            res = res.filterKeys { !regTmp.containsMatchIn(it) }
        }

        val regTmp1 = "摘要".toRegex()

        res = if (isAbstract) res.filterKeys { regTmp1.containsMatchIn(it) } else res.filterKeys { !regTmp1.containsMatchIn(it) }

        return res
    }

    /**
     * @return map of title to the pdf-url
     */
    fun getPdfLinks(index: Int, year: Int? = null, quarter: Int? = null, ignoreOutdated: Boolean = false, isAbstract: Boolean = false): Map<String, String> {
        return getPdfLinks(indexToCode(index), year, quarter, ignoreOutdated, isAbstract)
    }

    fun getEntityInfoById(query: String, cnt: Int = 60): Map<String, EntityInfo> {

        val requestData = """ticker=$query&limit=$cnt&date=365"""
        val url = GenericUrl("http://xbrl.cninfo.com.cn/do/stockreserch/getcompanybyprefix")
        val request = requestFactory!!.buildPostRequest(url, ByteArrayContent.fromString(null, requestData))

        request.headers.contentType = "application/x-www-form-urlencoded"
        return request.execute().parseAsString().split(",").map {
            val t = it.split("#")
            val tmp = updateGeneralDesc(t[2])
            t[0] to EntityInfo(t[0], t[0], t[1], t[3], t[4], orgName = t[2], orgNameEN = tmp.getValue("英文名称")
                    , location = tmp.getValue("办公地址"), url = tmp.getValue("公司网址"), email = tmp.getValue("电子信箱"), boardSecretary = tmp.getValue("董事会秘书姓名")
                    , emailBoardSecretary = tmp.getValue("董事会秘书电子信箱"), registeredCapital = try {
                NumberFormat.getInstance().parse(tmp.getValue("注册资本(万元)")).toDouble()
            } catch (e: Exception) {
                0.0
            }, securityDelegator = tmp.getValue("证券事务代表姓名")
                    , auditor = tmp.getValue("会计师事务所")
            )
        }.toMap()
    }


    // http://xbrl.cninfo.com.cn/do/summary/companyinfo
    private fun updateGeneralDesc(orgName: String): Map<String, String> {
        with(
                requestFactory!!.buildPostRequest(GenericUrl("http://xbrl.cninfo.com.cn/do/summary/companyinfo"),
                        ByteArrayContent.fromString(null, "orgname=${URLEncoder.encode(orgName, "UTF-8")}"))
        ) {
            this.headers.contentType = "application/x-www-form-urlencoded"
            with(Jsoup.parse(this.execute().parseAsString()).getElementById("overview")) {
                return this.getElementsByClass("th").map { it.ownText() }.zip(
                        this.getElementsByClass("td1").map { it.ownText() }
                ).toMap()
            }
        }
    }


    //http://xbrl.cninfo.com.cn/do/shareholder/shareholders?ticker=000002

    //http://xbrl.cninfo.com.cn/do/sincerelycase/getpunishmentdate?ticker=000002&page=1
    //http://xbrl.cninfo.com.cn/do/sincerelycase/getsincerelycase?ticker=000002&date=2016-08-05&index=0

    //http://xbrl.cninfo.com.cn/do/dividend/getdividendhistory?ticker=000002&page=1

    fun getEntityFacets(SECCode: String): String {
        val requestData = """ticker=$SECCode"""
        val url = GenericUrl("http://xbrl.cninfo.com.cn/do/generalinfo/getcompanygeneralinfo")
        val request = requestFactory!!.buildPostRequest(url, ByteArrayContent.fromString(null, requestData))

        request.headers.contentType = "application/x-www-form-urlencoded"
        return request.execute().parseAsString()
    }
}

