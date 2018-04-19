package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.ss.usermodel.Row

class ExcelStructureLoader(val path: String, val sheetIndex: Int = 0, val sheetName: String? = null, val keyCol: Int = 1, val secondaryKeyCol: Int = 2) : StructureLoader {
    override fun load(): List<ProtoAccount> {
        var res: MutableList<ProtoAccount> = mutableListOf()
        var tmpAggregateAccount: ProtoAccount? = null

        val f: (Row) -> Unit = {

            it.getCell(keyCol - 1)?.stringCellValue?.trim()?.let {
                if (!it.isEmpty()) {
                    val t1 = parseAccount(it)

                    tmpAggregateAccount?.let { res.add(it) }
                    tmpAggregateAccount = ProtoAccount(id = t1.first, name = t1.second)
                }
            }

            it.getCell(secondaryKeyCol - 1)?.stringCellValue?.trim()?.let {
                if (!it.isEmpty()) {
                    val t1 = parseAccount(it)
                    tmpAggregateAccount?.add(ProtoAccount(id = t1.first, name = t1.second, value = 0L))
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