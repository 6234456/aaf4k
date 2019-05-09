package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.flatList
import eu.qiou.aaf4k.util.io.ECBFxProvider
import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.template.Template
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.toIndexedMap
import eu.qiou.aaf4k.util.unit.ForeignExchange
import org.junit.Test
import java.time.LocalDate

class ForeignExchangeTest {
    @Test
    fun getDecimalPrecision() {
        ForeignExchange.autoFetch = true
        val fx = ForeignExchange(reportingCurrencyCode = "CHF", timePoint = LocalDate.of(2018, 5, 16))
        val fx1 = ForeignExchange(reportingCurrencyCode = "GBP", timePoint = LocalDate.of(2018, 5, 16))
        val fx2 = ForeignExchange(reportingCurrencyCode = "USD", timePoint = LocalDate.of(2018, 5, 16))
        println(fx)
        println(fx1)
        println(fx2)
    }

    @Test
    fun getNewFx() {
        val fx = ForeignExchange(reportingCurrencyCode = "CNY", timeSpan = TimeSpan(LocalDate.of(2018, 10, 30), LocalDate.now()))
        val fx1 = ECBFxProvider.baseFx(fx)
        println(fx)
        println(fx1)

        ECBFxProvider.toXls(fx, "data/trail_FX.xls")

    }

    @Test
    fun getBaseFx() {
        val f = ECBFxProvider.baseFx(ForeignExchange(reportingCurrencyCode = "CNY", timeParameters = TimeParameters.forYear(2017)))

        Template(
                headings = listOf
                (
                        Template.HeadingFormat("Date", ExcelUtil.DataFormat.STRING.format, "yyyy-mm-dd"),
                        Template.HeadingFormat("Exchange Rate", ExcelUtil.DataFormat.STRING.format, "#,###.0000")
                ),
                data = f.flatList().map { it.toIndexedMap() },
                sumRowBottom = Template.HeadingFormat("Average", formatData = "#,###.0000"),
                sumRowBottomFormula = "AVERAGE"
        ).build("src/eu/qiou/aaf4k/test/demo2.xlsx", "trail")

        println(ForeignExchange(reportingCurrencyCode = "CNY", timeParameters = TimeParameters.forYear(2017)))
    }

    @Test
    fun getTrail() {
        println(mapOf(1 to listOf<Int?>(1, 2, null, 4, 5)).flatList())
        println(mapOf(1 to 2).flatList())
    }
}