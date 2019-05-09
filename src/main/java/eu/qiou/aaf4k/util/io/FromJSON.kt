package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.reportings.base.*
import eu.qiou.aaf4k.reportings.base.Account.Companion.parseReportingType
import eu.qiou.aaf4k.util.time.TimeAttribute
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.time.parseTimeAttribute
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.time.LocalDate
import java.util.*
import kotlin.math.roundToInt


object FromJSON {

    fun account(json: JSONObject): ProtoAccount {

        val hasSubAccounts = json["hasSubAccounts"] as Boolean
        val validate = json["validateUntil"]

        val date = if (validate == null) null else LocalDate.parse(validate as String)


        return if (hasSubAccounts) {
            CollectionAccount(
                    id = json["id"] as Long,
                    name = json["name"] as String,
                    desc = json["desc"] as String,
                    decimalPrecision = (json["decimalPrecision"] as Long).toInt(),
                    isStatistical = json["isStatistical"] as Boolean,
                    validateUntil = date,
                    reportingType = parseReportingType(json.get("reportingType") as String)
            ).apply {
                (json["subAccounts"] as JSONArray).forEach {
                    account(it as JSONObject)
                }
            }
        } else {
            Account(
                    id = json["id"] as Long,
                    name = json["name"] as String,
                    desc = json["desc"] as String,
                    isStatistical = json["isStatistical"] as Boolean,
                    validateUntil = date,
                    decimalPrecision = (json["decimalPrecision"] as Long).toInt(),
                    reportingType = parseReportingType(json["reportingType"] as String)
                    ).copyWith(value = json["value"] as Double, decimalPrecision = (json["decimalPrecision"] as Long).toInt())
        }

    }

    fun entry(json: JSONObject, category: Category): Entry {
        return Entry(
                desc = json["desc"] as String,
                category = category,
                date = if (json["date"] == null) category.timeParameters.end else LocalDate.parse(json["date"] as String)
        ).apply {
            id = (json["id"] as Long).toInt()
            (json["accounts"] as JSONArray).forEach {
                add(account(it as JSONObject) as Account)
            }

            this.isActive = (json["isActive"] as Boolean?) ?: true
            this.isWritable = (json["isWritable"] as Boolean?) ?: true
            this.isVisible = (json["isVisible"] as Boolean?) ?: true
        }
    }

    fun category(json: JSONObject, reporting: Reporting): Category {
        val cons = json["consolidationCategory"]
        val consCat = if (cons == null) null else ConsolidationCategory.values().find {
            it.token == (cons as Long).toInt()
        }

        return Category(
                name = json["name"] as String,
                desc = json["desc"] as String,
                reporting = reporting,
                consolidationCategory = consCat
        ).apply {
            id = (json["id"] as Long).toInt()
            (json["entries"] as JSONArray).forEach {
                it as JSONObject
                // id 0 is reserved for the balance entry, omitted
                if ((it["id"] as Long).toInt() != 0)
                    entry(it, this)
            }

            this.isWritable = (json.get("isWritable") as Boolean?) ?: true
            //this.nextEntryIndex = (json.get("nextEntryIndex") as Long?)?.toInt() ?: 1
        }
    }

    fun person(json: JSONObject): Person {
        val dob = json.get("dateOfBirth")

        val date = if (dob == null) null else LocalDate.parse(dob as String)

        return Person(
                id = (json.get("id") as Long).toInt(),
                familyName = json.get("familyName") as String,
                givenName = json.get("givenName") as String,
                isMale = json.get("isMale") as Boolean,
                dateOfBirth = date,
                email = (json.get("email") as JSONArray).map {
                    it as String
                }.toMutableList(),
                phone = (json.get("phone") as JSONArray).map {
                    it as String
                }.toMutableList(),
                title = (json.get("title") as JSONArray).map {
                    it as String
                }.toMutableList()
        )
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

    fun address(json: JSONObject): Address {
        return Address(
                id = (json.get("id") as Long).toInt(),
                country = Locale.Builder().setRegion(json.get("country") as String).build(),
                province = json.get("province") as String,
                city = json.get("city") as String,
                zipCode = json.get("zipCode") as String,
                street = json.get("street") as String,
                number = json.get("number") as String
        )
    }

    fun entity(json: JSONObject): Entity {

        val p = json.get("contactPerson")
        val ps = if (p == null) null else person(p as JSONObject)

        val a = json.get("address")
        val ads = if (a == null) null else address(a as JSONObject)

        val c = json.get("child")
        val child = if (c == null) null else (c as JSONArray)
                .map { entity((it as JSONObject).get("key") as JSONObject) to it.get("value") as Double }.toMap()

        return Entity(
                id = json.get("id") as Long,
                name = json.get("name") as String,
                desc = json.get("desc") as String,
                abbreviation = json.get("abbreviation") as String,
                contactPerson = ps,
                address = ads
        ).apply {
            child?.forEach { t, u ->
                add(t, (u * 10000).roundToInt())
            }
        }
    }

    fun reporting(json: JSONObject): Reporting {
        return Reporting(
                CollectionAccount(
                    id = json["id"] as Long,
                    name = json["name"] as String,
                    desc = json["desc"] as String,
                    entity = entity(json["entity"] as JSONObject),
                    timeParameters = timeParameters(json["timeParameters"] as JSONObject)
                ).apply {
                    (json["structure"] as JSONArray).forEach {
                        add(account(it as JSONObject))
                    }
                }
        ).apply {
            (json["categories"] as JSONArray).forEach {
                category(it as JSONObject, this).apply {
                    summarizeResult()
                }
            }
        }

    }



    fun read(json: String): JSONObject {
        return JSONParser().parse(json) as JSONObject
    }
}

fun String.toAccount(): ProtoAccount = FromJSON.account(FromJSON.read(this))
fun String.toEntry(category: Category): Entry = FromJSON.entry(FromJSON.read(this), category)
fun String.toCategory(reporting: Reporting): Category = FromJSON.category(FromJSON.read(this), reporting)
fun String.toTimeParameters(): TimeParameters = FromJSON.timeParameters(FromJSON.read(this))
fun String.toPerson(): Person = FromJSON.person(FromJSON.read(this))
fun String.toAddress(): Address = FromJSON.address(FromJSON.read(this))
fun String.toEntity(): Entity = FromJSON.entity(FromJSON.read(this))
fun String.toReporting(): Reporting = FromJSON.reporting(FromJSON.read(this))
