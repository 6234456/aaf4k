package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.reportings.AggregateAccount
import eu.qiou.aaf4k.reportings.ProtoAccount
import eu.qiou.aaf4k.reportings.ProtoReporting
import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.ss.usermodel.Row

class ExcelLoader(var path:String, var sheetIndex: Int = 0, var sheetName: String? = null): DataLoader, StructureLoader {
    override fun loadStructure(reporting: ProtoReporting):ProtoReporting{
        var tmpAggregateAccount:AggregateAccount? = null

        val f: (Row) -> Unit = {
            var c1 = ""
            var c2 = ""

            if (it.getCell(0) != null)
                c1 = it.getCell(0).stringCellValue.trim()

            if (it.getCell(1) != null)
                c2 = it.getCell(1).stringCellValue.trim()

            if(c1.length == 0 && c2.length == 0){

            }else{
                if(c1.length != 0){
                    val t1 = parseAccount(c1)

                    if(tmpAggregateAccount != null){
                        reporting.addAggreateAccount(tmpAggregateAccount!!)
                    }

                    tmpAggregateAccount = AggregateAccount(id=t1.first, name = t1.second)
                }else{
                    val t1 = parseAccount(c2)
                    tmpAggregateAccount?.addSubAccount(ProtoAccount(id = t1.first, name = t1.second))
                }
            }
        }

        ExcelUtil.loopThroughRows(path, sheetIndex, sheetName, f)

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

    override fun loadData(): Map<Int, Double> {
        return mapOf()
    }
}