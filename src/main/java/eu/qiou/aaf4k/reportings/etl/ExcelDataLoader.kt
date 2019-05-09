package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.ss.usermodel.Row

class ExcelDataLoader(var path:String, var sheetIndex: Int = 0, var sheetName: String? = null, var keyCol:Int = 1, var valCol:Int = 2, var hasHeading:Boolean = false): DataLoader {
    override fun load(): MutableMap<Long, Double> {
        val res: MutableMap<Long, Double> = mutableMapOf()
        val f:(Row) -> Unit = {

            if (it.getCell(0) != null && it.getCell(1) != null && it.getCell(0).stringCellValue.trim().length > 0 ){
                var c1 = it.getCell(0).numericCellValue.toLong()
                var c2 = it.getCell(1).numericCellValue

                res.put(c1, c2)
            }
        }

        ExcelUtil.loopThroughRows(path=path,sheetIndex = sheetIndex, sheetName = sheetName, callback = f)

        return res
    }
}