package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.JSONUtil
import org.junit.Test

import org.junit.Assert.*

class JSONUtilTest {

    @Test
    fun fetchOne() {
        val url = "https://www.oanda.com/fx-for-business/historical-rates/api/data/update/?&source=OANDA&adjustment=0&base_currency=CNY&start_date=2017-10-8&end_date=2018-4-6&period=daily&price=bid&view=graph&quote_currency_0=EUR"

        println(JSONUtil.fetchOne<String>(url, false,"frequency"))
    }
}