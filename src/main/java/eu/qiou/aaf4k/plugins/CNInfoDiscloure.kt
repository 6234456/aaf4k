package eu.qiou.aaf4k.plugins

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import eu.qiou.aaf4k.util.strings.recode
import eu.qiou.aaf4k.util.time.TimeSpan
import org.jsoup.Jsoup

object CNInfoDiscloure {
    var requestFactory = NetHttpTransport().createRequestFactory()

    fun clear() {
        requestFactory = null
    }

    // http://www.cninfo.com.cn/information/management/szmb000002.html
    fun management(code: String): List<List<String>> {
        with(
                requestFactory.buildGetRequest(GenericUrl("http://www.cninfo.com.cn/information/management/${code}.html"))
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
                    val codet = Regex("""\/(\w+?\.html)(?:(?:"|')?;?)$""").find(this.getElementsByTag("script").get(0).html())!!.groupValues[1]

                    with(Jsoup.parse(
                            requestFactory.buildGetRequest(GenericUrl("http://www.cninfo.com.cn/information/${type.typeName}/$codet"))
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