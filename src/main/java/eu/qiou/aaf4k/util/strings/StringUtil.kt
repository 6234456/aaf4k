package eu.qiou.aaf4k.util.strings

import eu.qiou.aaf4k.util.strings.StringUtil.repeatString
import java.nio.charset.Charset

object StringUtil {
    fun repeatString(times: Int, token: String ="\t"):String{
        return if (times <= 0) ""
        else token + repeatString(times - 1, token)
    }
}

operator fun String.times(times: Int) = repeatString(times = times, token = this)

fun String.recode(decodeWith: String, encodeWith: String): String = this.toByteArray(Charset.forName(decodeWith)).toString(Charset.forName(encodeWith))
