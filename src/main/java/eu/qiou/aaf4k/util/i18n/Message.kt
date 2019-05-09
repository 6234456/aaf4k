package eu.qiou.aaf4k.util.i18n

import eu.qiou.aaf4k.reportings.GlobalConfiguration
import java.util.*

data class Message(val locale: Locale = GlobalConfiguration.DEFAULT_LOCALE) {
    companion object {
        private val m = Message()
        fun of(string: String): String = m.of(string)
    }

    private val msg = ResourceBundle.getBundle("aaf4k", locale)

    fun of(string: String): String = msg.getString(string)
}