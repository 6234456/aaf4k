package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.base.*
import eu.qiou.aaf4k.util.time.TimeParameters
import org.junit.Test
import java.util.*

class ReportingTest {

    val reporting = Reporting(CollectionAccount(123, "Demo", 3,
            timeParameters = TimeParameters.forYear(2020), entity = Entity(2345, "DemoAG", "DAG"))).apply {
        add(CollectionAccount(234, "Acc1").apply {
            add(Account(2341, "Trail1", value = 0, decimalPrecision = 3))
            add(Account(2342, "Trail2", value = 0, decimalPrecision = 3))
            add(Account(2343, "Trail3", value = 0, decimalPrecision = 3))
            add(Account(2345, "Trail4", value = 0, decimalPrecision = 3))
        })
        add(Account(235, "Trail0", value = 0, decimalPrecision = 3))
        prepareConsolidation(Locale.GERMAN)
        Entry("Demo", categoryRevenueExpenseCons!!).apply {
            add(2341, 12.0)
            add(2342, 12.0)
        }
        Entry("Demo1", categoryRevenueExpenseCons!!).apply {
            add(2341, 12.0)
            add(2342, 12.0)
        }
    }

    @Test
    fun getCategories() {
        assert(reporting.consCategoriesAdded)
        assert(reporting.categories.count() == 5)
        assert(reporting.categoryInitEquityCons != null)
    }

    @Test
    fun getEntity() {
        assert(reporting.entity.abbreviation == "DAG")
    }

    @Test
    fun add() {
        Account(2341, "Trail1", value = 123100, decimalPrecision = 4).let {
            println(it.displayValue)
            println(it.textValue)
        }
    }

    @Test
    fun toDataMap() {
        assert(reporting.categoryRevenueExpenseCons!!.toDataMap()[2342] == 12.0)
    }

    @Test
    fun checkDuplicate() {
        assert(reporting.generate().findAccountByID(2342)!!.decimalValue == (12.0 + 33.14))
    }

    @Test
    fun shorten() {
        println(reporting.categoryRevenueExpenseCons!!.toDataMap())
        println((reporting.deepCopy() as Reporting).categories.map { it.id })
        //    reporting.categoryRevenueExpenseCons!!.entries[1].id = 10
        println(reporting.categoryRevenueExpenseCons!!.entries.map { "${it.id} ${it.desc}" })

        println((reporting.deepCopy() as Reporting).categoryRevenueExpenseCons!!.entries[0].id)
        println(reporting.categoryRevenueExpenseCons!!.deepCopy(Reporting(CollectionAccount(123456, "Demo", timeParameters = TimeParameters.forYear(2020)))).toDataMap())
        //    assert(reporting.shorten().findAccountByID(2345) == null)
        println(reporting.findAccountByID(234)!!.shorten(whiteList = listOf(2342)))
        println(reporting.shorten())
    }

    @Test
    fun deepCopy() {
    }

    @Test
    fun generate() {
    }

    @Test
    fun findAccountByID() {
    }

    @Test
    fun nullify() {
    }

    @Test
    fun removeAccount() {
    }

    @Test
    fun addAccountTo() {
    }

    @Test
    fun clearCategories() {
    }

    @Test
    fun carryForward() {
    }

    @Test
    fun search() {
    }

    @Test
    fun updateList() {
    }
}