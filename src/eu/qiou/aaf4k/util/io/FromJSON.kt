package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.accounting.model.Account
import eu.qiou.aaf4k.accounting.model.Account.Companion.parseReportingType
import eu.qiou.aaf4k.accounting.model.Category
import eu.qiou.aaf4k.accounting.model.Entry
import eu.qiou.aaf4k.accounting.model.Reporting
import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.util.time.TimeAttribute
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.time.parseTimeAttribute
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.time.LocalDate


object FromJSON {

    fun account(json: JSONObject): Account {

        val hasSubAccounts = json.get("hasSubAccounts") as Boolean

        return if (hasSubAccounts) {
            Account.from(
                    ProtoAccount(
                            id = (json.get("id") as Long).toInt(),
                            name = json.get("name") as String,
                            subAccounts =
                            (json.get("subAccounts") as JSONArray).map {
                                account(it as JSONObject)
                            } as MutableList<Account>,
                            desc = json.get("desc") as String,
                            decimalPrecision = (json.get("decimalPrecision") as Long).toInt(),
                            isStatistical = json.get("isStatistical") as Boolean
                    ),

                    parseReportingType(json.get("reportingType") as String))
        } else {
            Account.from(
                    ProtoAccount.Builder(
                            id = (json.get("id") as Long).toInt(),
                            name = json.get("name") as String,
                            desc = json.get("desc") as String,
                            isStatistical = json.get("isStatistical") as Boolean
                    ).setValue(v = json.get("value") as Double, decimalPrecision = (json.get("decimalPrecision") as Long).toInt())
                            .build(),

                    parseReportingType(json.get("reportingType") as String))
        }

    }

    fun entry(json: JSONObject, category: Category): Entry {
        return Entry(id = (json.get("id") as Long).toInt(),
                desc = json.get("desc") as String,
                category = category
        ).apply {
            (json.get("accounts") as JSONArray).forEach {
                add(account(it as JSONObject))
            }
        }
    }

    fun category(json: JSONObject, reporting: Reporting): Category {
        return Category(
                id = (json.get("id") as Long).toInt(),
                name = json.get("name") as String,
                desc = json.get("desc") as String,
                reporting = reporting
        ).apply {
            (json.get("entries") as JSONArray).forEach {
                add(entry(it as JSONObject, this))
            }
        }
    }

    fun timeParameters(json: JSONObject): TimeParameters {
        val type = parseTimeAttribute(json.get("type") as Long)

        return when (type) {
            TimeAttribute.TIME_POINT -> TimeParameters(timePoint = LocalDate.parse(json.get("end") as String))
            TimeAttribute.TIME_SPAN -> TimeParameters(timeSpan =
            TimeSpan(LocalDate.parse(json.get("start") as String), LocalDate.parse(json.get("end") as String)))
            TimeAttribute.CONSTANT -> TimeParameters(null, null)
        }
    }

    fun read(json: String): JSONObject {
        return JSONParser().parse(json) as JSONObject
    }
}
