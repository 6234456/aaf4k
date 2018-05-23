package eu.qiou.aaf4k.util.template

import eu.qiou.aaf4k.reportings.GlobalConfiguration.DEFAULT_FONT_NAME
import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.ss.usermodel.*


/**
 *  first ele of Triple is the value of the heading
 *  second the format of heading
 *  third the format of data
 */
open class Template(val headings: List<Triple<Any, String, String>>? = null, val data: List<List<Any>>) {
    companion object {
        val heading: (wb: Workbook) -> CellStyle = {
            ExcelUtil.StyleBuilder(it)
                    .fill(IndexedColors.ROYAL_BLUE.index)
                    .font(name = DEFAULT_FONT_NAME, color = IndexedColors.WHITE.index, bold = true)
                    .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    .borderStyle(down = BorderStyle.THICK, up = BorderStyle.MEDIUM, left = BorderStyle.THIN, right = BorderStyle.THIN)
                    .build()
        }

        val rowLight: (wb: Workbook) -> CellStyle = {
            ExcelUtil.StyleBuilder(it)
                    .alignment(vertical = VerticalAlignment.CENTER)
                    .font().borderStyle(up = BorderStyle.DASHED, down = BorderStyle.DASHED, left = BorderStyle.THIN, right = BorderStyle.THIN)
                    .build()
        }

        val rowDark: (wb: Workbook) -> CellStyle = {
            ExcelUtil.StyleBuilder(it).fill(IndexedColors.PALE_BLUE.index)
                    .alignment(vertical = VerticalAlignment.CENTER)
                    .font().borderStyle(up = BorderStyle.DASHED, down = BorderStyle.DASHED, left = BorderStyle.THIN, right = BorderStyle.THIN)
                    .build()
        }
    }

    fun build(path: String, sheetName: String = "Overview") {
        val cols = data[0].count()
        val rows = data.count()
        val colWidth = 10
        val headingHeight = 35f
        val rowHeight = 15f

        ExcelUtil.createWorksheetIfNotExists(path, sheetName, {
            it.isDisplayGridlines = false

            val style = ExcelUtil.StyleBuilder(it.workbook).fromStyle(Template.heading(it.workbook))
                    .borderColor(left = IndexedColors.WHITE.index, right = IndexedColors.WHITE.index)
                    .build()

            with(it.createRow(0)) {
                for (i: Int in 1..cols) {
                    with(this.createCell(i - 1)) {
                        this.row.sheet.setColumnWidth(this.columnIndex, colWidth * 256)
                        ExcelUtil
                                .Update(this)
                                .style(style)
                                .borderColor(left = if (i == 1) IndexedColors.BLACK.index else null)
                                .borderStyle(left = if (i == 1) BorderStyle.MEDIUM else null)
                                .borderColor(right = if (i == cols) IndexedColors.BLACK.index else null)
                                .borderStyle(right = if (i == cols) BorderStyle.MEDIUM else null)
                                .dataFormat(this@Template.headings!!.get(i - 1).second)
                                .value(this@Template.headings.get(i - 1).first)
                    }
                }
                this.heightInPoints = headingHeight
            }

            var cnt = 1
            val light = rowLight(it.workbook)
            val dark = rowDark(it.workbook)


            this@Template.data.forEach({ v ->
                with(it.createRow(cnt++)) {
                    this.heightInPoints = rowHeight

                    v.forEachIndexed { index, d ->
                        with(this.createCell(index)) {
                            ExcelUtil.Update(this)
                                    .style(if (cnt % 2 == 0) light else dark)
                                    .dataFormat(this@Template.headings!!.get(index).third)
                                    .value(d)

                            if (cnt.equals(data.count() + 1)) {
                                ExcelUtil.Update(this).borderStyle(down = BorderStyle.MEDIUM)
                            }

                            if (index.equals(v.count() - 1)) {
                                ExcelUtil.Update(this).borderStyle(right = BorderStyle.MEDIUM)
                            }

                            if (index.equals(0)) {
                                ExcelUtil.Update(this).borderStyle(left = BorderStyle.MEDIUM)
                            }
                        }
                    }
                }
            })
        })
    }
}