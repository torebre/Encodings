package com.kjipo

import com.kjipo.representation.Line
import com.kjipo.representation.LineUtilities
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.css.*
import kotlinx.html.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val client = HttpClient() {

        //        install(JsonFeature) {
//            serializer = GsonSerializer()
//        }

        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()

            }
        }

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

    val unicodeToSegmentCount: Map<Int, Int> by lazy {
        segments.keys.groupBy { it.first }.map { Pair(it.key, it.value.size) }.toMap()
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

        get("/kanji/{unicode}/segmentnumber/{segment}") {

            // TODO

            val segmentData = segments[Pair(call.parameters["unicode"]?.toInt(), call.parameters["segment"]?.toInt())]
            if (segmentData == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respondText { segmentData.map { it.toString() }.joinToString("\n") }
            }
        }

        get("/kanji/{unicode}/segments") {
            val count = unicodeToSegmentCount[call.parameters["unicode"]?.toInt()]
            if(count == null) {
                call.respond(HttpStatusCode.NotFound)
            }
            else {
                call.respondText { count.toString() }
            }
        }

        get("/kanji/{unicode}/segment/{segment}/matrix") {
            val segmentData = segments[Pair(call.parameters["unicode"]?.toInt(), call.parameters["segment"]?.toInt())]
            if (segmentData == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                val drawLines = LineUtilities.drawLines(segmentData)

//                call.defaultTextContentType(ContentType.Application.Json)
                call.respond(drawLines)

            }
        }

        get("/kanji/{unicode}/segment/{segment}/drawing") {
            val segmentData = segments[Pair(call.parameters["unicode"]?.toInt(), call.parameters["segment"]?.toInt())]
            if (segmentData == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                val drawLines = LineUtilities.drawLines(segmentData)
                val stringBuilder = StringBuilder()
                for (ints in drawLines.array) {
                    ints.forEach {
                        if(it == 0) {
                            stringBuilder.append(" ")
                        }
                        else {
                            stringBuilder.append("x")
                        }

                    }
                    stringBuilder.append("\n")

                }

//                call.defaultTextContentType(ContentType.Application.Json)
                call.respondText { stringBuilder.toString() }

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
