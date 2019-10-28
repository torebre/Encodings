package com.kjipo

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import io.ktor.client.*
import io.ktor.features.CORS
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val client = HttpClient() {
    }

    val segments: Map<Pair<Int, Int>, List<Line>> by lazy {
        val allLines = Files.readAllLines(Paths.get("kanji_data_segments.csv"))
        allLines.subList(1, allLines.size)
                .map {
                    val splitString = it.split(",")
                    val key = splitString[0].toInt()
                    val segment = splitString[6].toInt()

                    Pair(Pair(key, segment), Line(splitString[1].toInt(),
                            splitString[2].toDouble(),
                            splitString[3].toDouble(),
                            splitString[4].toInt(),
                            splitString[5].toInt()))
                }
                .groupBy {
                    it.first
                }.map {
                    Pair(it.key, it.value.map { it.second })
                }.toMap()
    }

    install(CORS) {
        anyHost()
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/html-dsl") {
            call.respondHtml {
                body {
                    h1 { +"HTML" }
                    ul {
                        for (n in 1..10) {
                            li { +"$n" }
                        }
                    }
                }
            }
        }

        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.red
                }
                p {
                    fontSize = 2.em
                }
                rule("p.myclass") {
                    color = Color.blue
                }
            }
        }

        get("/kanji") {
            call.respondText(Files.list(Paths.get("kanji_output8"))
                    .map { file ->
                        file.toFile().name.substringBefore('.')
                    }.collect(Collectors.joining(",")),
                    contentType = ContentType.Text.Plain)
        }

        get("/kanji/{unicode}") {
            call.respondFile(Paths.get("kanji_output8").resolve("${call.parameters["unicode"]}.dat").toFile())
        }

        get("/kanji/{unicode}/segment/{segment}") {
            val segmentData = segments[Pair(call.parameters["unicode"]?.toInt(), call.parameters["segment"]?.toInt())]
            if (segmentData == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respondText { segmentData.map { it.toString() }.joinToString("\n") }
            }
        }
    }
}

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
