package com.kjipo

import com.kjipo.Constants.segmentData
import com.kjipo.representation.Line
import com.kjipo.representation.LineUtilities
import com.kjipo.representation.SegmentLine
import com.kjipo.skeleton.transformArraysToMatrix
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

    val segments by lazy {
        loadLines()
    }

    val unicodeToSegmentCount: Map<Int, Int> by lazy {
        segments.keys.groupBy { it.first }.map { Pair(it.key, it.value.size) }.toMap()
    }

    val linesWithSegments by lazy {
        loadSegmentLines()
    }


    install(CORS) {
        anyHost()
    }

    routing {

        get("/kanji") {
            call.respondText(segments.map { it.key.first }
                    .distinct().joinToString(",") { it.toString() },
                    contentType = ContentType.Text.Plain)
        }

        get("/kanji/{unicode}/linedata") {
            val lines = Files.readAllLines(Paths.get("linedata/kanji_line_data_${call.parameters["unicode"]}.csv")).map {
                val splitString = it.split(",")

                Line(splitString[1].toInt(),
                        splitString[2].toDouble(),
                        splitString[3].toDouble(),
                        splitString[4].toInt(),
                        splitString[5].toInt())
            }
                    .toList()
            call.respond(lines)
        }

        get("/kanji/{unicode}") {
            call.respondFile(Paths.get("kanji_output8").resolve("${call.parameters["unicode"]}.dat").toFile())
        }

        get("/kanji/{unicode}/matrix") {
            val unicode = call.parameters["unicode"] ?: return@get
            val kanjiFile = Paths.get("kanji_output8").resolve("${unicode}.dat")
            val loadEncodedKanjiFromString = loadEncodedKanjiFromString(Files.readAllLines(kanjiFile))

            call.respond(transformArraysToMatrix(loadEncodedKanjiFromString))
        }

        get("/kanji/{unicode}/segment/{segment}") {
            val segmentData = segments[Pair(call.parameters["unicode"]?.toInt(), call.parameters["segment"]?.toInt())]
            if (segmentData == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respondText { segmentData.map { it.toString() }.joinToString("\n") }
            }
        }

        get("/kanji/{unicode}/segment/{segment}/matrix") {
            val segmentData = segments[Pair(call.parameters["unicode"]?.toInt(), call.parameters["segment"]?.toInt())]
            if (segmentData == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                val drawLines = LineUtilities.drawLines(segmentData)
                call.respond(drawLines)
            }
        }

        get("/kanji/{unicode}/segment/{segment}/drawing") {
            val segmentData = segments[Pair(call.parameters["unicode"]?.toInt(), call.parameters["segment"]?.toInt())]
            if (segmentData == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                val drawLines = LineUtilities.drawLines(segmentData)

                with(StringBuilder()) {
                    for (ints in drawLines.array) {
                        ints.forEach { value ->
                            if (value == 0) {
                                append(" ")
                            } else {
                                append("x")
                            }
                        }
                        append("\n")
                    }
                    call.respondText { toString() }
                }

            }
        }

        get("/kanji/{unicode}/segmentdata") {
            val data = linesWithSegments[call.parameters["unicode"]?.toInt()]

            if (data == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(data)
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
