package eu.qiou.aaf4k.util.template

import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Workbook

open class Template {
    companion object {
        val heading: (wb: Workbook) -> CellStyle = {
            ExcelUtil.StyleBuilder(it)
                    .fill(IndexedColors.DARK_BLUE.index).font(color = IndexedColors.WHITE.index, bold = true)
                    .build()
        }

        val rowEven: (wb: Workbook) -> CellStyle = {
            ExcelUtil.StyleBuilder(it)
                    .font().borderStyle(up = BorderStyle.DOTTED, down = BorderStyle.DOTTED, left = BorderStyle.HAIR, right = BorderStyle.HAIR)
                    .build()
        }

        val rowOdd: (wb: Workbook) -> CellStyle = {
            ExcelUtil.StyleBuilder(it).fill(IndexedColors.LIGHT_BLUE.index)
                    .font().borderStyle(up = BorderStyle.DOTTED, down = BorderStyle.DOTTED, left = BorderStyle.HAIR, right = BorderStyle.HAIR)
                    .build()
        }
    }
}