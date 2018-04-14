package eu.qiou.aaf4k.reportings.model

import java.time.LocalDate

data class Person(val id:Int, var firstName:String, var givenName:String, var isMale:Boolean = true, var dateOfBirth: LocalDate? = null,
                  var email: MutableSet<String>?= mutableSetOf(), var phone: MutableSet<String>?= mutableSetOf(), var title: MutableSet<String>? = mutableSetOf())