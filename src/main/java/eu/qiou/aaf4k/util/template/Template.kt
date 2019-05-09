package eu.qiou.aaf4k.util.template

import eu.qiou.aaf4k.reportings.GlobalConfiguration.DEFAULT_FONT_NAME
import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellUtil


open class Template(val headings: List<HeadingFormat>? = null, val data: List<Map<*, *>>, val caption: List<Pair<String, String>>? = null, val colorSchema: ColorSchema = ColorSchema(),
                    val sumColRight: HeadingFormat? = null, val sumRowBottom: HeadingFormat? = null, val sumRowBottomFormula: String = "SUM", val sumColRightFormula: String = "SUM", var theme: Theme? = null) {
    class ColorSchema(val colorHeading: IndexedColors = IndexedColors.ROYAL_BLUE, val colorDarkRow: IndexedColors = IndexedColors.PALE_BLUE)

    // the formula param if not null depend on other columns
    // [1] + [2]   relative reference
    // (1) + (2)   absolute reference
    class HeadingFormat(val value: Any = "", val formatHeading: String = ExcelUtil.DataFormat.STRING.format,
                        val formatData: String = ExcelUtil.DataFormat.NUMBER.format,
                        val dataAggregatable: Boolean = false,
                        val bindingKeyinData: String? = null, val formula: String? = null,
                        val isAutoIncrement: Boolean = false,
                        val columnWidth: Int = 24)

    enum class Theme(val dark: Long, val light: Long) {
        DEFAULT(11892015L, 16247773L),
        BLACK_WHITE(7434613L, 14277081L),
        LAVANDA(10498160L, 16306927L),
        ORANGE(3243501L, 14083324L),
        SKY_BLUE(15773696L, 16247773L),
        LIGHT_GREEN(9359529L, 14348258L),
        BLOOD(3620091L, 9147389L)
    }

    companion object {
        fun heading(wb: Workbook, theme: Theme = Theme.DEFAULT): CellStyle {
            return ExcelUtil.StyleBuilder(wb)
                    .fillLong(theme.dark)
                    .font(name = DEFAULT_FONT_NAME, color = IndexedColors.WHITE.index, bold = true)
                    .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    .borderStyle(down = BorderStyle.THICK, up = BorderStyle.MEDIUM, left = BorderStyle.THIN, right = BorderStyle.THIN)
                    .build()
        }

        fun rowLight(wb: Workbook): CellStyle {
            return ExcelUtil.StyleBuilder(wb)
                    .alignment(vertical = VerticalAlignment.CENTER)
                    .font()
                    .borderStyle(up = BorderStyle.DASHED, down = BorderStyle.DASHED, left = BorderStyle.THIN, right = BorderStyle.THIN)
                    .build()
        }

        fun rowDark(wb: Workbook, theme: Theme = Theme.DEFAULT): CellStyle {
            return ExcelUtil.StyleBuilder(wb)
                    .fromStyle(rowLight(wb))
                    .fillLong(theme.light)
                    .build()
        }
    }

    fun build(path: String, sheetName: String = "Overview") {
        val cols = headings?.size ?: data[0].count()
        val colWidth = 24
        val headingHeight = 45f
        val rowHeight = 20f
        val captionHeight = 30f

        val rowStart = (caption?.count() ?: -1) + 1
        val colStart = 0

        val extraCol = if (sumColRight != null) 1 else 0
        val extraRow = if (sumRowBottom != null) 1 else 0


        ExcelUtil.createWorksheetIfNotExists(path, sheetName, {
            it.isDisplayGridlines = false
            val dark = ExcelUtil.StyleBuilder(it.workbook).fromStyle(rowDark(it.workbook), false)
                    .apply {
                        if (theme == null)
                            this.fill(colorSchema.colorDarkRow.index)
                        else
                            this.fill(theme!!.light)
                    }
                    .build()

            val w = it.workbook
            val light = rowLight(w)

            val heading = ExcelUtil.StyleBuilder(it.workbook).fromStyle(heading(it.workbook), false)
                    .borderColor(left = IndexedColors.WHITE.index, right = IndexedColors.WHITE.index)
                    .apply {
                        if (theme == null)
                            this.fill(colorSchema.colorHeading.index)
                        else
                            this.fill(theme!!.dark)
                    }
                    .build()

            if (this.caption != null) {
                val caption = ExcelUtil.StyleBuilder(it.workbook).fromStyle(heading, false)
                        .borderStyle(BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE)
                        .dataFormat(ExcelUtil.DataFormat.STRING.format)
                        .build()

                this.caption.forEachIndexed { index, list ->
                    with(it.createRow(index)) {
                        this.heightInPoints = captionHeight
                        for (i: Int in 1 + colStart..cols + colStart + extraCol) {
                            with(this.createCell(i - 1)) {
                                ExcelUtil
                                        .Update(this)
                                        .style(ExcelUtil.StyleBuilder(w).fromStyle(caption)
                                                .alignment(when (i) {
                                                    1 + colStart -> HorizontalAlignment.LEFT
                                                    cols + colStart + extraCol -> HorizontalAlignment.RIGHT
                                                    else -> HorizontalAlignment.CENTER
                                                }).build())
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
                        this.row.sheet.setColumnWidth(this.columnIndex, ((headings?.get(i - 1 - colStart)?.columnWidth)
                                ?: colWidth) * 256)
                        ExcelUtil
                                .Update(this)
                                .style(
                                        ExcelUtil.StyleBuilder(w).fromStyle(heading)
                                                .borderColor(left = if (i == 1 + colStart) IndexedColors.BLACK.index else null)
                                                .borderStyle(left = if (i == 1 + colStart) BorderStyle.MEDIUM else null)
                                                .borderColor(right = if (i == cols + colStart + extraCol) IndexedColors.BLACK.index else null)
                                                .borderStyle(right = if (i == cols + colStart + extraCol) BorderStyle.MEDIUM else null)
                                                .dataFormat(if (extraCol == 1 && i == cols + colStart + extraCol) sumColRight!!.formatHeading else this@Template.headings!!.get(i - 1 - colStart).formatHeading)
                                                .build()
                                )
                                .value(if (extraCol == 1 && i == cols + colStart + extraCol) sumColRight!!.value else this@Template.headings!!.get(i - 1 - colStart).value)
                    }
                }
                this.heightInPoints = headingHeight
            }

            var cnt = rowStart + 1
            val orderedHeadings = this@Template.headings?.map { k -> k.bindingKeyinData ?: k.value.toString() }

            this@Template.data.forEach { v0 ->
                with(it.createRow(cnt++)) {
                    this.heightInPoints = rowHeight

                    // transform the Map into List, the values
                    val v = orderedHeadings?.map { k -> v0.getOrDefault(k, "") } ?: (v0.values)

                    v.forEachIndexed { index, d ->
                        with(this.createCell(index + colStart)) {
                            val c = this
                            ExcelUtil.Update(this)
                                    .style(ExcelUtil.StyleBuilder(w).fromStyle(if ((cnt - rowStart) % 2 == 0) light else dark)
                                            .dataFormat(this@Template.headings!!.get(index).formatData)
                                            .borderStyle(
                                                    down = if (cnt.equals(data.count() + 1 + rowStart)) BorderStyle.MEDIUM else null,
                                                    right = if (index.equals(v.count() - 1)) BorderStyle.MEDIUM else null,
                                                    left = if (index.equals(0)) BorderStyle.MEDIUM else null
                                            )
                                            .build()
                                    ).apply {
                                        (headings[index]).let{ e->
                                            if (e.isAutoIncrement)
                                                value(cnt - 1 - rowStart)
                                            else if (e.formula == null)
                                                value(d)
                                            else
                                                formula(parseFormula(c, e.formula))
                                        }
                                    }

                        }
                    }

                    if (extraCol == 1) {
                        with(this.createCell(v.count() + colStart)) {
                            ExcelUtil.Update(this)
                                    .style(ExcelUtil.StyleBuilder(w).fromStyle(if ((cnt - rowStart) % 2 == 0) light else dark)
                                    .borderColor(right = IndexedColors.BLACK.index)
                                    .borderStyle(right = BorderStyle.MEDIUM)
                                            .dataFormat(sumColRight!!.formatData)
                                            .build())
                                    .formula("${sumColRightFormula}(${CellUtil.getCell(this.row, colStart + 1).address}:${CellUtil.getCell(this.row, this.columnIndex - 1).address})")
                        }
                    }
                }
            }

            if (extraRow == 1) {
                with(it.createRow(cnt++)) {
                    this.heightInPoints = rowHeight
                    for (i: Int in 1 + colStart..cols + colStart + extraCol) {
                        with(this.createCell(i - 1)) {
                            ExcelUtil.Update(this).style(ExcelUtil.StyleBuilder(w).fromStyle(heading)
                                    .borderStyle(down = BorderStyle.MEDIUM, up = BorderStyle.DOUBLE)
                                    .borderStyle(left = if (i == 1 + colStart) BorderStyle.MEDIUM else null)
                                    .borderColor(left = if (i == 1 + colStart) IndexedColors.BLACK.index else null)
                                    .borderStyle(right = if (i == cols + colStart + extraCol) BorderStyle.MEDIUM else null)
                                    .dataFormat(if (i == 1 + colStart) sumRowBottom!!.formatHeading else sumRowBottom!!.formatData)
                                    .alignment(if (i == 1 + colStart) null else HorizontalAlignment.RIGHT)
                                    .borderColor(right = if (i == cols + colStart + extraCol) IndexedColors.BLACK.index else null).build())
                                    .formula(if (this@Template.headings?.get(i - 1 - colStart)?.dataAggregatable != true) null else
                                        "$sumRowBottomFormula(${CellUtil.getCell(CellUtil.getRow(rowStart + 1, this.sheet), this.columnIndex).address}:${CellUtil.getCell(CellUtil.getRow(this.rowIndex - 1, this.sheet), this.columnIndex).address})"
                                    )
                                    .value(if (i == 1 + colStart) sumRowBottom.value else null)
                        }
                    }
                }
            }
        })
    }

    private fun parseFormula(c: Cell, formulaString: String): String {
        val relativeReg = """\[\s*(-?\d+)\s*]""".toRegex()
        val absoluteReg = """\(\s*(\d+)\s*\)""".toRegex()

        return formulaString.replace(relativeReg) {
            val v = it.groups[1]!!.value.toInt()
            "${CellUtil.getCell(c.row, c.columnIndex + v).address}"
        }.replace(absoluteReg) {
            val v = it.groups[1]!!.value.toInt()
            "${CellUtil.getCell(c.row, v - 1).address}"
        }
    }
}