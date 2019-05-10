package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.ss.usermodel.Row

class ExcelDataLoader(var path:String, var sheetIndex: Int = 0, var sheetName: String? = null, var keyCol:Int = 1, var valCol:Int = 2, var hasHeading:Boolean = false): DataLoader {
    override fun load(): MutableMap<Long, Double> {
        val res: MutableMap<Long, Double> = mutableMapOf()
        val f:(Row) -> Unit = {
            if (! (hasHeading && it.rowNum < 1)){
                val idCol = keyCol - 1
                val valueCol = valCol - 1

                if (it.getCell(idCol) != null && it.getCell(valueCol) != null && it.getCell(idCol).stringCellValue.isNotBlank()){
                    val c1 = it.getCell(idCol).numericCellValue.toLong()
                    val c2 = it.getCell(valueCol).numericCellValue

                    res[c1] = c2
                }
            }
        }

        ExcelUtil.loopThroughRows(path=path, sheetIndex = sheetIndex, sheetName = sheetName, callback = f)

        return res
    }
}