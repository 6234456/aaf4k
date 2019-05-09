package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.unit.CurrencyUnit
import eu.qiou.aaf4k.util.unit.UnitScalar
import org.junit.Test
import java.time.LocalDate
import java.util.*

class CurrencyUnitTest {

    @Test
    fun convertTo() {
        val unit1 = CurrencyUnit(scalar = UnitScalar.BILLION, currency = Currency.getInstance(Locale.CHINA))
        val unit2 = CurrencyUnit(scalar = UnitScalar.BILLION)

        assert(unit1 == unit2)
        assert(unit1.equals(unit2))

        val span1 = TimeSpan(LocalDate.now(), LocalDate.of(2018, 4, 3))
        val span2 = TimeSpan(LocalDate.now(), LocalDate.now())

        assert(span1 == span2)

    }
}