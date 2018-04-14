package eu.qiou.aaf4k.reportings.model

import java.util.*

data class Address(val id:Int, var country:Locale, var pronvice:String, var city:String, var zipCode:String, var street:String, var number: String)