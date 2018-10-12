package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.reportings.etl.ExcelDataLoader
import eu.qiou.aaf4k.reportings.model.ProtoAccount
import org.junit.Test


class ExcelStructureLoaderTest {

    @Test
    fun loadStructure() {
        val data = ExcelDataLoader("src/eu/qiou/aaf4k/demo/data.xlsx").load()
        val acc = ProtoAccount(0, "test1")
        val subAcc1 = ProtoAccount.builder().setValue(2.0).setBasicInfo(400, "a").build()

        acc.add(subAcc1)
        println(acc)

        val acc1 = acc.deepCopy<ProtoAccount>(data)

        println(acc1.value)
        println(acc1.decimalValue)
        println(acc1.hasSuperAccounts)
        println(acc1.hasSubAccounts)

        println(subAcc1.hasSuperAccounts)
        println(subAcc1.decimalValue)
    }
}