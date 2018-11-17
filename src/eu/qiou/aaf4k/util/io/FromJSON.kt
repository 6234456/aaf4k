package eu.qiou.aaf4k.util.io

import eu.qiou.aaf4k.accounting.model.Account
import eu.qiou.aaf4k.accounting.model.Account.Companion.parseReportingType
import eu.qiou.aaf4k.accounting.model.Category
import eu.qiou.aaf4k.accounting.model.Entry
import eu.qiou.aaf4k.accounting.model.Reporting
import eu.qiou.aaf4k.reportings.model.Address
import eu.qiou.aaf4k.reportings.model.Person
import eu.qiou.aaf4k.reportings.model.ProtoAccount
import eu.qiou.aaf4k.reportings.model.ProtoEntity
import eu.qiou.aaf4k.util.time.TimeAttribute
import eu.qiou.aaf4k.util.time.TimeParameters
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.time.parseTimeAttribute
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.time.LocalDate
import java.util.*


object FromJSON {

    fun account(json: JSONObject): Account {

        val hasSubAccounts = json.get("hasSubAccounts") as Boolean
        val validate = json.get("validateUntil")

        val date = if (validate == null) null else LocalDate.parse(validate as String)


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
                            isStatistical = json.get("isStatistical") as Boolean,
                            validateUntil = date
                    ),

                    parseReportingType(json.get("reportingType") as String))
        } else {
            Account.from(
                    ProtoAccount.Builder(
                            id = (json.get("id") as Long).toInt(),
                            name = json.get("name") as String,
                            desc = json.get("desc") as String,
                            isStatistical = json.get("isStatistical") as Boolean,
                            validateUntil = date
                    ).setValue(v = json.get("value") as Double, decimalPrecision = (json.get("decimalPrecision") as Long).toInt())
                            .build(),

                    parseReportingType(json.get("reportingType") as String))
        }

    }

    fun entry(json: JSONObject, category: Category): Entry {
        return Entry(id = (json.get("id") as Long).toInt(),
                desc = json.get("desc") as String,
                category = category,
                date = if (json.get("date") == null) category.timeParameters.end else LocalDate.parse(json.get("date") as String)
        ).apply {
            (json.get("accounts") as JSONArray).forEach {
                add(account(it as JSONObject))
            }

            this.isActive = (json.get("isActive") as Boolean?) ?: true
            this.isWritable = (json.get("isWritable") as Boolean?) ?: true
            this.isVisible = (json.get("isVisible") as Boolean?) ?: true
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
                it as JSONObject
                // id 0 is reserved for the balance entry, omitted
                if ((it.get("id") as Long).toInt() != 0)
                    entry(it, this)
            }

            this.isWritable = (json.get("isWritable") as Boolean?) ?: true
            this.nextEntryIndex = (json.get("nextEntryIndex") as Long?)?.toInt() ?: 1
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

    fun entity(json: JSONObject): ProtoEntity {

        val p = json.get("contactPerson")
        val ps = if (p == null) null else person(p as JSONObject)

        val a = json.get("address")
        val ads = if (a == null) null else address(a as JSONObject)

        val c = json.get("child")
        val child = if (c == null) null else (c as JSONArray).map { entity(it as JSONObject) }

        val pr = json.get("parent")
        val parent = if (pr == null) null else (pr as JSONArray).map { entity(it as JSONObject) }


        return ProtoEntity(
                id = (json.get("id") as Long).toInt(),
                name = json.get("name") as String,
                desc = json.get("desc") as String,
                abbreviation = json.get("abbreviation") as String,
                contactPerson = ps,
                address = ads
        ).apply {
            parentEntitis = parent?.toMutableList()
        }
    }

    fun reporting(json: JSONObject): Reporting {
        return Reporting(
                id = (json.get("id") as Long).toInt(),
                name = json.get("name") as String,
                desc = json.get("desc") as String,
                structure = (json.get("structure") as JSONArray).map {
                    account(it as JSONObject)
                } as MutableList<Account>,
                entity = entity(json.get("entity") as JSONObject),
                timeParameters = timeParameters(json.get("timeParameters") as JSONObject)
        ).apply {
            (json.get("categories") as JSONArray).forEach {
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

fun String.toAccount(): Account = FromJSON.account(FromJSON.read(this))
fun String.toEntry(category: Category): Entry = FromJSON.entry(FromJSON.read(this), category)
fun String.toCategory(reporting: Reporting): Category = FromJSON.category(FromJSON.read(this), reporting)
fun String.toTimeParamters(): TimeParameters = FromJSON.timeParameters(FromJSON.read(this))
fun String.toPerson(): Person = FromJSON.person(FromJSON.read(this))
fun String.toAddress(): Address = FromJSON.address(FromJSON.read(this))
fun String.toEntity(): ProtoEntity = FromJSON.entity(FromJSON.read(this))
fun String.toReporting(): Reporting = FromJSON.reporting(FromJSON.read(this))
