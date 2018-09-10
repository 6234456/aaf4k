package eu.qiou.aaf4k.plugins

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import eu.qiou.aaf4k.util.time.TimeSpan
import org.jsoup.Jsoup
import java.nio.charset.Charset

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
                return this.getElementsByClass("zx_data3").map { it.ownText().toByteArray(Charset.forName("ISO-8859-1")).toString(Charset.forName("GB2312")) }.chunked(4).zip(
                        this.getElementsByClass("zx_data2").map { it.ownText().toByteArray(Charset.forName("ISO-8859-1")).toString(Charset.forName("GB2312")) }
                ).map { it.first + it.second }
            }
        }
    }

    fun generalInfo(code: String): Map<String, String> {
        with(
                requestFactory.buildGetRequest(GenericUrl("http://www.cninfo.com.cn/information/brief/${code}.html"))
        ) {
            with(Jsoup.parse(this.execute().parseAsString())) {
                return this.getElementsByClass("zx_data").map { it.ownText().toByteArray(Charset.forName("ISO-8859-1")).toString(Charset.forName("GB2312")) }.zip(
                        this.getElementsByClass("zx_data2").map { it.ownText().toByteArray(Charset.forName("ISO-8859-1")).toString(Charset.forName("GB2312")) }
                ).toMap()
            }
        }
    }

    enum class FSType(val typeName: String, val url: String) {
        INCOME_STMT("incomestatements", "incomestatements"),
        BALANCE_STMT("balancesheet", "balancesheet")
    }

    fun fs(code: String, year: Int, quarter: Int, type: FSType): List<List<String>> {

        val d = TimeSpan.forQuarter(year, quarter).end.toString()
        val yyyy = year.toString()
        val mm = d.replace(yyyy, "")

        val reg = Regex("""^\D{4}""")
        with(
                requestFactory.buildPostRequest(GenericUrl("http://www.cninfo.com.cn/information/stock/${type.url}_.jsp?stockCode=${if (reg.matches(code)) reg.replace(code, "") else code}"),
                        ByteArrayContent.fromString(null, "yyyy=${yyyy}&mm=${mm}&cwzb=${type.typeName}")
                )
        ) {
            this.headers.contentType = "application/x-www-form-urlencoded"
            with(Jsoup.parse(this.execute().parseAsString())) {

                with(this.getElementsByClass("zx_left").get(0).getElementsByTag("td")
                        .map { it.text() }
                        .chunked(2)) {

                    return this.filterIndexed { i, _ -> i % 2 == 0 } + this.filterIndexed { i, _ -> i % 2 == 1 }
                }
            }
        }
    }
}