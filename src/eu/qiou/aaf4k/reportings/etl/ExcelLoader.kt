package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.reportings.AggregateAccount
import eu.qiou.aaf4k.reportings.ProtoAccount
import eu.qiou.aaf4k.reportings.ProtoReporting
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream

class ExcelLoader(var path:String, var sheetIndex: Int = 0, var sheetName: String? = null): DataLoader, StructureLoader {
    override fun loadStructure(reporting: ProtoReporting):ProtoReporting{

        val inputStream = FileInputStream(path)
        val wb = if (path.endsWith(".xls"))
                    HSSFWorkbook(inputStream)
                else
                    XSSFWorkbook(inputStream)

        val sht = if(sheetName != null)
                wb.getSheet(sheetName)
            else
                wb.getSheetAt(sheetIndex)

        val rows = sht.rowIterator()

        var tmpAggregateAccount:AggregateAccount? = null

        while (rows.hasNext()){
            val row = rows.next()

            var c1 = ""
            var c2 = ""

            if (row.getCell(0) != null)
                c1 = row.getCell(0).stringCellValue.trim()

            if (row.getCell(1) != null)
                c2 = row.getCell(1).stringCellValue.trim()

            if(c1.length == 0 && c2.length == 0){
                continue
            }else{
                if(c1.length != 0){
                    val t1 = parseAccount(c1)

                    if(tmpAggregateAccount != null){
                        reporting.addAggreateAccount(tmpAggregateAccount)
                    }

                    tmpAggregateAccount = AggregateAccount(id=t1.first, name = t1.second)
                }else{
                    val t1 = parseAccount(c2)
                    tmpAggregateAccount?.addSubAccount(ProtoAccount(id = t1.first, name = t1.second))
                }
            }
        }

        inputStream.close()

        reporting.addAggreateAccount(tmpAggregateAccount!!)

        return  reporting
    }

    private fun parseAccount(content:String):Pair<Int, String>{
        val reg1 = Regex("""^\d+\s+""")
        val reg2 = Regex("""\s+""")

        if(!reg1.containsMatchIn(content)){
            throw Exception("Ill-Formed Account by '" + content + "'")
        }

        val (a, b) = reg2.split(content,2)
        return Pair(a.toInt(), b)
    }

    override fun loadData(): Map<Int, Long> {
        return mapOf()
    }
}