package eu.qiou.aaf4k.reportings.model

import eu.qiou.aaf4k.util.io.JSONable
import java.util.*

data class Address(val id: Int, var country: Locale, var province: String, var city: String, var zipCode: String, var street: String, var number: String) : JSONable {
    override fun toJSON(): String {
        return """{"id":$id, "country":"${country.country}", "province":"$province", "city":"$city", "zipCode":"$zipCode", "street":"$street", "number":"$number"}"""
    }

}