package eu.qiou.aaf4k.util.strings

import eu.qiou.aaf4k.util.strings.StringUtil.repeatString

object StringUtil {
    fun repeatString(times: Int, token: String ="\t"):String{
        if(times <= 0) return ""
        else return token + repeatString(times - 1, token)
    }
}

operator fun String.times(times: Int) = repeatString(times = times, token = this)
