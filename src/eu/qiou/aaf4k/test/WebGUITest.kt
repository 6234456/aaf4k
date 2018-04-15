package eu.qiou.aaf4k.test

import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.unit.ForeignExchange
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

object WebGUITest {

    @JvmStatic
    fun main(args: Array<String>) {
        val server = embeddedServer(Netty, 8080) {
            routing {
                get("/{month}") {
                    call.respondText(ForeignExchange("EUR","CNY", TimeSpan.forMonth(2018,call.parameters["month"]!!.toInt())).toString(), ContentType.Text.Plain)
                }
            }
        }
        server.start(wait = true)
    }
}