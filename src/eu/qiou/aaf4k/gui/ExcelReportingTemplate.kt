package eu.qiou.aaf4k.gui

import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.ss.usermodel.CellType

class ExcelReportingTemplate(val tpl: String,
                             val prefix: String = "[", val affix: String = "]",
                             val shtName: String? = null, val shtIndex: Int = 0) {

    fun export(data: Map<*, *>, path: String) {
        val (wb, ips) = ExcelUtil.getWorkbook(tpl)
        val sht = if (shtName != null) {
            wb.getSheet(shtName)
        } else
            wb.getSheetAt(shtIndex)

        val engine = TemplateEngine(data, prefix, affix)

        sht.rowIterator().forEach {
            it.cellIterator().forEach {
                if (it.cellTypeEnum == CellType.STRING) {
                    if (engine.containsTemplate(it.stringCellValue)) {
                        val v = engine.parse(it.stringCellValue)

                        try {
                            it.setCellValue(v.toDouble())
                        } catch (e: Exception) {
                            it.setCellValue(v)
                        }
                    }
                }
            }
        }

        ExcelUtil.saveWorkbook(path, wb)
        wb.close()
        ips.close()
    }
}