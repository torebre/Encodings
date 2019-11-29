package com.kjipo.viewer

import KanjiViewer
import com.github.aakira.napier.Napier
import com.kjipo.representation.EncodedKanji
import com.kjipo.representation.Matrix
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document


class KanjiApp {
    private val canvas = document.getElementById("canvas") as HTMLCanvasElement

    private val context = canvas.getContext("2d")
    private val kanjiViewer = KanjiViewer(context as CanvasRenderingContext2D)


    fun show() {
        canvas.height = 500
        canvas.width = 500


        val request = XMLHttpRequest()
        request.open("GET", "http://0.0.0.0:8094/kanji/33253", false)
        request.send()

        val response = request.response as String

        console.log("Response: $response")

        val loadedKanji = loadEncodedKanjiFromString(response, 33253)

        console.log("Loaded kanji: $loadedKanji")
        console.log("Canvas: $canvas")
        console.log("Context: $context")

        kanjiViewer.setupKanjiDrawing(loadedKanji)
    }


    fun loadEncodedKanjiFromString(kanjiString: String, unicode: Int) = EncodedKanji(loadEncodedKanjiFromString(kanjiString.split('\n')), unicode)

    fun loadEncodedKanjiFromString(kanjiString: List<String>): Array<BooleanArray> {
        return kanjiString.map {
            it.map {
                if (it.equals('1')) {
                    true
                } else if (it.equals('0')) {
                    false
                } else {
                    null
                }
            }
                    .filterNotNull()
                    .toBooleanArray()
        }.toTypedArray()
    }


    fun loadSegmentData() {
        val client = HttpClient(Js)

        CoroutineScope(Dispatchers.Main).launch {
            val numberOfSegments = client.get<String>("http://0.0.0.0:8094/kanji/27355/segments").toInt()

            // TODO Not using all segments when testing
            for (i in 0 until numberOfSegments) {
                console.log("Loading segment: $i")

                try {
                    val response = client.get<String>("http://0.0.0.0:8094/kanji/27355/segment/$i/matrix")
                    val parsedResponse = JSON.parse<Matrix<Int>>(response)
                    val element = document.createElement("canvas") as HTMLCanvasElement

                    document.body?.appendChild(element)

                    val context2 = element.getContext("2d")
                    val kanjiViewer2 = KanjiViewer(context2 as CanvasRenderingContext2D)

                    with(element) {
                        setAttribute("width", (2 * parsedResponse.numberOfColumns).toString())
                        setAttribute("height", (2 * parsedResponse.numberOfRows).toString())
                    }

                    kanjiViewer2.setupKanjiDrawing(parsedResponse, 2)
                }
                catch (e: ClientRequestException) {
                    Napier.e("Could not find segment", e)
                    continue
                }


            }
        }


    }


}