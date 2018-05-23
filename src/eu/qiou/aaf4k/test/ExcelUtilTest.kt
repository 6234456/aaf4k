package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.io.ExcelUtil
import eu.qiou.aaf4k.util.template.Template
import eu.qiou.aaf4k.util.template.Template.Companion.rowDark
import eu.qiou.aaf4k.util.template.Template.Companion.rowLight
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.IndexedColors
import org.junit.Test
import java.time.LocalDate


class ExcelUtilTest {

    @Test
    fun writeData() {
        ExcelUtil.writeData("src/eu/qiou/aaf4k/test/demo1.xls", data = mapOf("1" to listOf(23.4, LocalDate.now()), "2" to listOf(234.123123)))
    }


    @Test
    fun createTmpl() {
        ExcelUtil.createWorksheetIfNotExists("src/eu/qiou/aaf4k/test/demo1.xlsx", "DemoTmpl", {

            it.isDisplayGridlines = false

            val style = ExcelUtil.StyleBuilder(it.workbook).fromStyle(Template.heading(it.workbook))
                    .dataFormat("mmm yyyy")
                    .borderColor(left = IndexedColors.WHITE.index, right = IndexedColors.WHITE.index)
                    .build()

            with(it.createRow(0)) {
                for (i: Int in 1..13) {
                    with(this.createCell(i - 1)) {

                        this.row.sheet.setColumnWidth(this.columnIndex, 10 * 256)

                        ExcelUtil
                                .Update(this)
                                .style(style)
                                .dataFormat(if (i == 1) ExcelUtil.DataFormat.STRING.format else "mmm yyyy")
                                .borderColor(left = if (i == 1) IndexedColors.BLACK.index else null)
                                .borderStyle(left = if (i == 1) BorderStyle.MEDIUM else null)
                                .borderColor(right = if (i == 13) IndexedColors.BLACK.index else null)
                                .borderStyle(right = if (i == 13) BorderStyle.MEDIUM else null)
                                .value(if (i == 1) "Accounts" else LocalDate.of(2018, i - 1, 1))
                    }
                }
                this.heightInPoints = 35f
            }

            val data = mutableMapOf<String, List<Double>>(
                    "Account1" to (1..12).map { it.toDouble() },
                    "Account2" to (1..12).map { it.toDouble() },
                    "Account3" to (1..12).map { it.toDouble() },
                    "Account4" to (1..12).map { it.toDouble() },
                    "Account5" to (1..12).map { it.toDouble() }
            )

            var cnt = 1
            val light = rowLight(it.workbook)
            val dark = rowDark(it.workbook)


            data.forEach({ k, v ->
                with(it.createRow(cnt++)) {

                    this.heightInPoints = 15f

                    with(this.createCell(0)) {
                        ExcelUtil.Update(this)
                                .style(if (cnt % 2 == 0) light else dark)
                                .borderStyle(left = BorderStyle.MEDIUM)
                                .value(k)

                        ExcelUtil.Update(this)
                                .borderStyle(down = if (cnt.equals(data.count() + 1)) BorderStyle.MEDIUM else null)
                    }

                    v.forEachIndexed { index, d ->
                        with(this.createCell(index + 1)) {
                            ExcelUtil.Update(this)
                                    .style(if (cnt % 2 == 0) light else dark)
                                    .dataFormat(ExcelUtil.DataFormat.NUMBER.format)
                                    .value(d)

                            if (cnt.equals(data.count() + 1)) {
                                ExcelUtil.Update(this).borderStyle(down = BorderStyle.MEDIUM)
                            }

                            if (index.equals(v.count() - 1)) {
                                ExcelUtil.Update(this).borderStyle(right = BorderStyle.MEDIUM)
                            }
                        }
                    }
                }
            })

        })
    }



    @Test
    fun formatExcel() {
        Template(
                listOf(
                        Triple("Name", ExcelUtil.DataFormat.STRING.format, ExcelUtil.DataFormat.STRING.format),
                        Triple(LocalDate.now(), ExcelUtil.DataFormat.DATE.format, ExcelUtil.DataFormat.NUMBER.format)
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