package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.JSONUtil
import eu.qiou.aaf4k.util.io.query
import eu.qiou.aaf4k.util.io.toDate
import org.json.simple.JSONArray
import org.junit.Test

class JSONUtilTest {

    @Test
    fun fetchOne() {
        val url = "https://www.oanda.com/fx-for-business/historical-rates/api/data/update/?&source=OANDA&adjustment=0&base_currency=CNY&start_date=2017-10-8&end_date=2018-4-6&period=daily&price=bid&view=graph&quote_currency_0=EUR"

        println(JSONUtil.fetch<Any>(url, false,"widget.0.data"))

        val obj = JSONUtil.readFromCache(url).query<JSONArray>("widget.0.data").map { e ->
            val tmp = (e as JSONArray)
            arrayListOf((tmp[0] as Long).toDate(), tmp[1].toString().toDouble())
        }

        println(obj)
    }
}