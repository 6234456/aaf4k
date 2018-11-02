package eu.qiou.aaf4k.util.template

import eu.qiou.aaf4k.reportings.GlobalConfiguration.DEFAULT_FONT_NAME
import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellUtil


open class Template(val headings: List<HeadingFormat>? = null, val data: List<List<*>>, val caption: List<Pair<String, String>>? = null, val colorSchema: ColorSchema = ColorSchema(),
                    val sumColRight: HeadingFormat? = null, val sumRowBottom: HeadingFormat? = null, val sumRowBottomFormula: String = "SUM", val sumColRightFormula: String = "SUM") {
    data class ColorSchema(val colorHeading: IndexedColors = IndexedColors.ROYAL_BLUE, val colorDarkRow: IndexedColors = IndexedColors.PALE_BLUE, val colorCaption: IndexedColors = colorHeading)
    data class HeadingFormat(val value: Any, val formatHeading: String = ExcelUtil.DataFormat.STRING.format, val formatData: String = ExcelUtil.DataFormat.NUMBER.format)


    companion object {

        var theme: Pair<Long, Long> = 11892015L to 16247773L

        val fillLight: (cell: Cell) -> Unit = {
            ExcelUtil.fillLong(it, Template.theme.second)
        }

        val fillDark: (cell: Cell) -> Unit = {
            ExcelUtil.fillLong(it, Template.theme.first)
        }

        val heading: (wb: Workbook) -> CellStyle = {
            ExcelUtil.StyleBuilder(it)
                    .fillLong(theme.first)
                    .font(name = DEFAULT_FONT_NAME, color = IndexedColors.WHITE.index, bold = true)
                    .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    .borderStyle(down = BorderStyle.THICK, up = BorderStyle.MEDIUM, left = BorderStyle.THIN, right = BorderStyle.THIN)
                    .build()
        }

        val rowLight: (wb: Workbook) -> CellStyle = {
            ExcelUtil.StyleBuilder(it)
                    .alignment(vertical = VerticalAlignment.CENTER)
                    .font()
                    .borderStyle(up = BorderStyle.DASHED, down = BorderStyle.DASHED, left = BorderStyle.THIN, right = BorderStyle.THIN)
                    .build()
        }

        val rowDark: (wb: Workbook) -> CellStyle = {
            ExcelUtil.StyleBuilder(it)
                    .fillLong(theme.second)
                    .alignment(vertical = VerticalAlignment.CENTER)
                    .font().borderStyle(up = BorderStyle.DASHED, down = BorderStyle.DASHED, left = BorderStyle.THIN, right = BorderStyle.THIN)
                    .build()
        }
    }

    fun build(path: String, sheetName: String = "Overview") {
        val cols = data[0].count()
        val colWidth = 10
        val headingHeight = 35f
        val rowHeight = 15f

        val rowStart = (caption?.count() ?: -1) + 1
        val colStart = 0

        val extraCol = if (sumColRight != null) 1 else 0
        val extraRow = if (sumRowBottom != null) 1 else 0


        ExcelUtil.createWorksheetIfNotExists(path, sheetName, {
            it.isDisplayGridlines = false
            val dark = ExcelUtil.StyleBuilder(it.workbook).fromStyle(rowDark(it.workbook)).fill(color = this.colorSchema.colorDarkRow.index).build()
            val light = rowLight(it.workbook)

            val heading = ExcelUtil.StyleBuilder(it.workbook).fromStyle(Template.heading(it.workbook))
                    .borderColor(left = IndexedColors.WHITE.index, right = IndexedColors.WHITE.index)
                    .fill(color = this@Template.colorSchema.colorHeading.index)
                    .build()

            if (this.caption != null) {
                val caption = ExcelUtil.StyleBuilder(it.workbook).fromStyle(heading)
                        .borderStyle(BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE)
                        .fill(color = this@Template.colorSchema.colorCaption.index)
                        .dataFormat(ExcelUtil.DataFormat.STRING.format)
                        .build()

                this.caption.forEachIndexed { index, list ->
                    with(it.createRow(index)) {
                        for (i: Int in 1 + colStart..cols + colStart + extraCol) {
                            with(this.createCell(i - 1)) {
                                ExcelUtil
                                        .Update(this)
                                        .style(caption)
                                        .alignment(when (i) {
                                            1 + colStart -> HorizontalAlignment.LEFT
                                            cols + colStart + extraCol -> HorizontalAlignment.RIGHT
                                            else -> HorizontalAlignment.CENTER
                                        })
                                        .value(when (i) {
                                            1 + colStart -> list.first
                                            cols + colStart + extraCol -> list.second
                                            else -> ""
                                        })
                            }
                        }
                    }
                }
            }

            with(it.createRow(rowStart)) {
                for (i: Int in 1 + colStart..cols + colStart + extraCol) {
                    with(this.createCell(i - 1)) {
                        this.row.sheet.setColumnWidth(this.columnIndex, colWidth * 256)
                        ExcelUtil
                                .Update(this)
                                .style(heading)
                                .borderColor(left = if (i == 1 + colStart) IndexedColors.BLACK.index else null)
                                .borderStyle(left = if (i == 1 + colStart) BorderStyle.MEDIUM else null)
                                .borderColor(right = if (i == cols + colStart + extraCol) IndexedColors.BLACK.index else null)
                                .borderStyle(right = if (i == cols + colStart + extraCol) BorderStyle.MEDIUM else null)
                                .dataFormat(if (extraCol == 1 && i == cols + colStart + extraCol) sumColRight!!.formatHeading else this@Template.headings!!.get(i - 1 - colStart).formatHeading)
                                .value(if (extraCol == 1 && i == cols + colStart + extraCol) sumColRight!!.value else this@Template.headings!!.get(i - 1 - colStart).value)
                    }
                }
                this.heightInPoints = headingHeight
            }

            var cnt = rowStart + 1


            this@Template.data.forEach({ v ->
                with(it.createRow(cnt++)) {
                    this.heightInPoints = rowHeight

                    v.forEachIndexed { index, d ->
                        with(this.createCell(index + colStart)) {
                            ExcelUtil.Update(this)
                                    .style(if ((cnt - rowStart) % 2 == 0) light else dark)
                                    .dataFormat(this@Template.headings!!.get(index).formatData)
                                    .value(d)

                            if (cnt.equals(data.count() + 1 + rowStart)) {
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

                    if (extraCol == 1) {
                        with(this.createCell(v.count() + colStart)) {
                            ExcelUtil.Update(this)
                                    .style(if ((cnt - rowStart) % 2 == 0) light else dark)
                                    .borderColor(right = IndexedColors.BLACK.index)
                                    .borderStyle(right = BorderStyle.MEDIUM)
                                    .dataFormat(sumColRight!!.formatData)
                                    .formula("${sumColRightFormula}(${CellUtil.getCell(this.row, colStart + 1).address}:${CellUtil.getCell(this.row, this.columnIndex - 1).address})")
                        }
                    }
                }
            })

            if (extraRow == 1) {
                with(it.createRow(cnt++)) {
                    for (i: Int in 1 + colStart..cols + colStart + extraCol) {
                        with(this.createCell(i - 1)) {
                            ExcelUtil.Update(this).style(heading)
                                    .borderStyle(down = BorderStyle.MEDIUM, up = BorderStyle.DOUBLE)
                                    .borderStyle(left = if (i == 1 + colStart) BorderStyle.MEDIUM else null)
                                    .borderColor(left = if (i == 1 + colStart) IndexedColors.BLACK.index else null)
                                    .borderStyle(right = if (i == cols + colStart + extraCol) BorderStyle.MEDIUM else null)
                                    .borderColor(right = if (i == cols + colStart + extraCol) IndexedColors.BLACK.index else null)
                                    .formula(if (i == 1 + colStart) null else
                                        "${sumRowBottomFormula}(${CellUtil.getCell(CellUtil.getRow(rowStart + 1, this.sheet), this.columnIndex).address}:${CellUtil.getCell(CellUtil.getRow(this.rowIndex - 1, this.sheet), this.columnIndex).address})"
                                    )
                                    .value(if (i == 1 + colStart) sumRowBottom!!.value else null)
                                    .dataFormat(if (i == 1 + colStart) sumRowBottom!!.formatHeading else sumRowBottom!!.formatData)
                                    .alignment(if (i == 1 + colStart) null else HorizontalAlignment.RIGHT)
                        }
                    }
                }
            }
        })
    }
}