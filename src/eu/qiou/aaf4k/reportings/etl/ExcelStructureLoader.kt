package eu.qiou.aaf4k.reportings.etl

import eu.qiou.aaf4k.reportings.base.Account
import eu.qiou.aaf4k.reportings.base.CollectionAccount
import eu.qiou.aaf4k.reportings.base.ProtoAccount
import eu.qiou.aaf4k.util.io.ExcelUtil
import org.apache.poi.ss.usermodel.Row

/**
 *  The Implementation of 2-level account structure differentiated with indent hierarchy
 */

class ExcelStructureLoader(val path: String, val sheetIndex: Int = 0, val sheetName: String? = null, val keyCol: Int = 1, val secondaryKeyCol: Int = 2) : StructureLoader {
    override fun load(): List<ProtoAccount> {
        val res: MutableList<ProtoAccount> = mutableListOf()
        var tmpAggregateAccount: CollectionAccount? = null

        val f: (Row) -> Unit = {

            it.getCell(keyCol - 1)?.stringCellValue?.trim()?.let {
                if (!it.isEmpty()) {
                    val t1 = parseAccount(it)

                    tmpAggregateAccount?.let { res.add(it) }
                    tmpAggregateAccount = CollectionAccount(id = t1.first, name = t1.second)
                }
            }

            it.getCell(secondaryKeyCol - 1)?.stringCellValue?.trim()?.let {
                if (!it.isEmpty()) {
                    val t1 = parseAccount(it)
                    tmpAggregateAccount?.add(Account(id = t1.first, name = t1.second, value = 0L))
                }
            }
        }

        ExcelUtil.loopThroughRows(path, sheetIndex, sheetName, f)

        res.add(tmpAggregateAccount!!)

        return res
    }

    /**
     * @param content the String represents the account like the form "1200 Account Receivables"
     */
    private fun parseAccount(content: String): Pair<Long, String> {
        val reg1 = Regex("""^\d+\s+""")
        val reg2 = Regex("""\s+""")

        if(!reg1.containsMatchIn(content)){
            throw Exception("Ill-Formed Account by '" + content + "'")
        }

        val (a, b) = reg2.split(content,2)
        return Pair(a.toLong(), b)
    }
}