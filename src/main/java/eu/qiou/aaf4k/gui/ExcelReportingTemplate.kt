package eu.qiou.aaf4k.gui

import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet

class ExcelReportingTemplate(private val tpl: String,
                             private val prefix: String = "[", private val affix: String = "]",
                             val shtName: String? = null, private val shtIndex: Int = 0, val fmt: String = "%.2f") {

    fun export(data: Map<*, *>, path: String, filter: (Sheet) -> Boolean = { if (shtName != null) it.sheetName == shtName else it.workbook.getSheetIndex(it) == shtIndex }) {
        val (wb, ips) = ExcelUtil.getWorkbook(tpl)
        val engine = TemplateEngine(prefix, affix, fmt)
        val d = data.map { it.key.toString() to it.value!! }.toMap()

        wb.sheetIterator().forEach { sht ->
            if (filter(sht)) {
                sht.rowIterator().forEach { x ->
                    x.cellIterator().forEach {
                        if (it.cellTypeEnum == CellType.STRING) {
                            if (engine.containsTemplate(it.stringCellValue)) {
                                val v = engine.compile(it.stringCellValue)(d)
                                try {
                                    it.setCellValue(v.toDouble())
                                } catch (e: Exception) {
                                    it.setCellValue(v)
                                }
                            }
                        }
                    }
                }
            }
        }


        wb.forceFormulaRecalculation = true
        ExcelUtil.saveWorkbook(path, wb)
        wb.close()
        ips.close()
    }
}