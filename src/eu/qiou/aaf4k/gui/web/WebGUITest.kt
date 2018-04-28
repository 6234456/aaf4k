package eu.qiou.aaf4k.gui.web

import eu.qiou.aaf4k.reportings.etl.ExcelDataLoader
import eu.qiou.aaf4k.reportings.etl.ExcelStructureLoader
import eu.qiou.aaf4k.reportings.model.ProtoReporting
import eu.qiou.aaf4k.util.strings.CollectionToString
import eu.qiou.aaf4k.util.time.TimeSpan
import eu.qiou.aaf4k.util.unit.ForeignExchange
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.*
import java.io.FileReader


object WebGUITest {

    @JvmStatic
    fun main(args: Array<String>) {
        val server = embeddedServer(Netty, 8080) {
            routing {
                get("/fx/{month}") {
                    call.respondText(ForeignExchange("EUR", "CNY", TimeSpan.forMonth(2018, call.parameters["month"]!!.toInt())).toString(), ContentType.Text.Plain)
                }
                get("/") {
                    call.respondText(FileReader("src/eu/qiou/aaf4k/gui/web/tmpl/main.html").readText(), ContentType.Text.Html)
                }
                get("/data") {
                    val structure = ExcelStructureLoader("src/eu/qiou/aaf4k/demo/DemoStructure.xlsx", sheetName = "Bilanz").load()
                    val data = ExcelDataLoader("src/eu/qiou/aaf4k/demo/data.xlsx", sheetName = "Bilanz").load()
                    val reporting = ProtoReporting(0, "Demo", structure = structure).update(data)
                    call.respondText(CollectionToString.mkJSON(reporting.flatten()), ContentType.Application.Json)
                }
                get("/main") {
                    call.respondHtml {
                        head {
                            link(rel = "stylesheet", type = "text/css", href = "https://docs.handsontable.com/pro/bower_components/handsontable-pro/dist/handsontable.full.min.css")
                            link(rel = "stylesheet", type = "text/css", href = "https://handsontable.com/static/css/main.css")
                            script(type = "text/javascript", src = "https://docs.handsontable.com/pro/bower_components/handsontable-pro/dist/handsontable.full.min.js") {}
                        }
                        body {
                            div(classes = "hot") { +"hello" }
                        }
                    }
                }
            }
        }

        server.start(wait = true)
    }
}