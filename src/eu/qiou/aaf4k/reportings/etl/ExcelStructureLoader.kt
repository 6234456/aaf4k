package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.ss.usermodel.Row

class ExcelStructureLoader(var path:String, var sheetIndex: Int = 0, var sheetName: String? = null): StructureLoader {
    override fun loadStructure(): List<ProtoAccount> {
        var res: MutableList<ProtoAccount> = mutableListOf()
        var tmpAggregateAccount: ProtoAccount? = null

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
                        res.add(tmpAggregateAccount!!)
                    }

                    tmpAggregateAccount = ProtoAccount(id = t1.first, name = t1.second)
                }else{
                    val t1 = parseAccount(c2)
                    tmpAggregateAccount?.add(ProtoAccount(id = t1.first, name = t1.second))
                }
            }
        }

        ExcelUtil.loopThroughRows(path, sheetIndex, sheetName, f)

        res.add(tmpAggregateAccount!!)

        return res
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
}