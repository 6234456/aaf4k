package eu.qiou.aaf4k.reportings.base

import eu.qiou.aaf4k.util.io.JSONable
import eu.qiou.aaf4k.util.mkString
import java.time.LocalDate

data class Person(val id: Int, var familyName: String, var givenName: String, var isMale: Boolean = true, var dateOfBirth: LocalDate? = null,
                  var email: MutableList<String>? = mutableListOf(), var phone: MutableList<String>? = mutableListOf(), var title: MutableList<String>? = mutableListOf()) : JSONable {
    override fun toJSON(): String {
        return """{"id":$id, "familyName":"$familyName", "givenName":"$givenName","isMale":$isMale, "dateOfBirth":$dateOfBirth, "email":${email?.map { "'$it'" }?.mkString()
                ?: "[]"}, "phone":${phone?.map { "'$it'" }?.mkString()
                ?: "[]"},"title":${title?.map { "'$it'" }?.mkString() ?: "[]"}}"""

    }

}