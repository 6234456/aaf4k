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
                                .borderColor(right = if (i == 13) IndexedColors.BLACK.index else null)
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
                                .borderStyle(down = if (cnt.equals(data.count() + 1)) BorderStyle.HAIR else null)
                                .value(k)
                    }

                    v.forEachIndexed { index, d ->
                        with(this.createCell(index + 1)) {
                            ExcelUtil.Update(this)
                                    .style(if (cnt % 2 == 0) light else dark)
                                    .dataFormat(ExcelUtil.DataFormat.NUMBER.format)
                                    .borderStyle(down = if (cnt.equals(data.count() + 1)) BorderStyle.HAIR else null)
                                    .value(d)
                        }
                    }
                }
            })

        })
    }



    @Test
    fun formatExcel() {
        ExcelUtil.createWorksheetIfNotExists("src/eu/qiou/aaf4k/test/demo1.xls", "Demo6", {
            val style = ExcelUtil.StyleBuilder(it.workbook).fromStyle(Template.heading(it.workbook)).dataFormat("mmm yyyy").build()
            with(it.createRow(0)) {
                with(this.createCell(0)) {
                    this.cellStyle = style
                    ExcelUtil.setCellValue(this, LocalDate.now())
                }
                with(this.createCell(1)) {
                    this.cellStyle = ExcelUtil.StyleBuilder(it.workbook).fromStyle(style).fill(IndexedColors.BLUE.index).dataFormat(ExcelUtil.DataFormat.STRING).build()
                    ExcelUtil.setCellValue(this, "BLUE")
                }
                with(this.createCell(2)) {
                    this.cellStyle = ExcelUtil.StyleBuilder(it.workbook).fromStyle(style).fill(IndexedColors.LIGHT_CORNFLOWER_BLUE.index).dataFormat(ExcelUtil.DataFormat.STRING).build()
                    ExcelUtil.setCellValue(this, "LIGHT_CORNFLOWER_BLUE")
                }
                with(this.createCell(3)) {
                    this.cellStyle = ExcelUtil.StyleBuilder(it.workbook).fromStyle(style).fill(IndexedColors.BLUE1.index).dataFormat(ExcelUtil.DataFormat.STRING).build()
                    ExcelUtil.setCellValue(this, "BLUE1E")
                }
                with(this.createCell(4)) {
                    this.cellStyle = ExcelUtil.StyleBuilder(it.workbook).fromStyle(style).fill(IndexedColors.BLUE_GREY.index).dataFormat(ExcelUtil.DataFormat.STRING).build()
                    ExcelUtil.setCellValue(this, "BLUE_GREY")
                }
                with(this.createCell(5)) {
                    this.cellStyle = ExcelUtil.StyleBuilder(it.workbook).fromStyle(style).fill(IndexedColors.AQUA.index).dataFormat(ExcelUtil.DataFormat.STRING).build()
                    ExcelUtil.setCellValue(this, "AQUA")
                }
                with(this.createCell(6)) {
                    this.cellStyle = ExcelUtil.StyleBuilder(it.workbook).fromStyle(style).fill(IndexedColors.PALE_BLUE.index).dataFormat(ExcelUtil.DataFormat.STRING).build()
                    ExcelUtil.setCellValue(this, "PALE_BLUE")
                }
                with(this.createCell(7)) {
                    this.cellStyle = ExcelUtil.StyleBuilder(it.workbook).fromStyle(style).fill(IndexedColors.ROYAL_BLUE.index).dataFormat(ExcelUtil.DataFormat.STRING).build()
                    ExcelUtil.setCellValue(this, "ROYAL_BLUE")
                }
                with(this.createCell(8)) {
                    this.cellStyle = ExcelUtil.StyleBuilder(it.workbook).fromStyle(style).fill(IndexedColors.CORNFLOWER_BLUE.index).dataFormat(ExcelUtil.DataFormat.STRING).build()
                    ExcelUtil.setCellValue(this, "CORNFLOWER_BLUE")
                }
                with(this.createCell(9)) {
                    this.cellStyle = ExcelUtil.StyleBuilder(it.workbook).fromStyle(style).fill(IndexedColors.SKY_BLUE.index).dataFormat(ExcelUtil.DataFormat.STRING).build()
                    ExcelUtil.setCellValue(this, "SKY_BLUE")
                }
            }
        })
    }
}