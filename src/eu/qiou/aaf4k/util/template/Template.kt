package eu.qiou.aaf4k.util.template

import eu.qiou.aaf4k.reportings.GlobalConfiguration.DEFAULT_FONT_NAME
import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.ss.usermodel.*

open class Template {
    companion object {
        val heading: (wb: Workbook) -> CellStyle = {
            ExcelUtil.StyleBuilder(it)
                    .fill(IndexedColors.ROYAL_BLUE.index)
                    .font(name = DEFAULT_FONT_NAME, color = IndexedColors.WHITE.index, bold = true)
                    .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    .borderStyle(down = BorderStyle.THICK, up = BorderStyle.THIN, left = BorderStyle.THIN, right = BorderStyle.THIN)
                    .build()
        }

        val rowLight: (wb: Workbook) -> CellStyle = {
            ExcelUtil.StyleBuilder(it)
                    .alignment(vertical = VerticalAlignment.CENTER)
                    .font().borderStyle(up = BorderStyle.DOTTED, down = BorderStyle.DOTTED, left = BorderStyle.THIN, right = BorderStyle.THIN)
                    .build()
        }

        val rowDark: (wb: Workbook) -> CellStyle = {
            ExcelUtil.StyleBuilder(it).fill(IndexedColors.PALE_BLUE.index)
                    .alignment(vertical = VerticalAlignment.CENTER)
                    .font().borderStyle(up = BorderStyle.DOTTED, down = BorderStyle.DOTTED, left = BorderStyle.THIN, right = BorderStyle.THIN)
                    .build()
        }
    }
}