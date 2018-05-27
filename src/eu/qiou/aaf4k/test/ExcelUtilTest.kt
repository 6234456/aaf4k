package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.template.Template
import org.junit.Test
import java.time.LocalDate


class ExcelUtilTest {

    @Test
    fun writeData() {
        ExcelUtil.writeData("src/eu/qiou/aaf4k/test/demo1.xls", data = mapOf("1" to listOf(23.4, LocalDate.now()), "2" to listOf(234.123123)))
    }

    @Test
    fun formatExcel() {
        Template(
                listOf(
                        Triple("Name", ExcelUtil.DataFormat.STRING.format, ExcelUtil.DataFormat.STRING.format),
                        Triple(LocalDate.now(), "mmm yyyy", ExcelUtil.DataFormat.NUMBER.format)
                ),
                listOf(
                        listOf("Hello", -12345),
                        listOf("Hello2", 12345.98),
                        listOf("Hello3", 1231145),
                        listOf("Hello4", 1231145),
                        listOf("Hello5", 112345),
                        listOf("Hello6", 112345),
                        listOf("Hello7", 123415)
                )

        ).build("src/eu/qiou/aaf4k/test/demo1.xls")
    }
}