package eu.qiou.aaf4k.util.template

import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType

open class Template {
    companion object {
        val headingFormatBlue: (cellStyle: CellStyle, cell: Cell) -> Unit = { style, c ->
            style.fillForegroundColor = ExcelUtil.setColor(c.sheet.workbook as HSSFWorkbook, 91, 155.toByte(), 213.toByte()).index
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
            style.setFont(c.sheet.workbook.createFont().apply {
                color = HSSFColor.WHITE.index
                fontName = "Arial"
                bold = true
            })
        }
        val rowFormatBlue: (cellStyle: CellStyle, cell: Cell) -> Unit = { style, c ->
            if (c.rowIndex.rem(2) == 0) {
                style.fillForegroundColor = ExcelUtil.setColor(c.sheet.workbook as HSSFWorkbook, 221.toByte(), 235.toByte(), 247.toByte()).index
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
                style.setFont(c.sheet.workbook.createFont().apply {
                    fontName = "Arial"
                })
            }

            style.setBorderBottom(BorderStyle.DOTTED)
        }
    }


}