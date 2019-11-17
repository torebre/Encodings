package com.kjipo.viewer

import Bounds
import KanjiViewer
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document
import com.kjipo.representation.EncodedKanji
import com.kjipo.representation.Matrix
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class KanjiApp {
    private val canvas = document.getElementById("canvas") as HTMLCanvasElement

    private val context = canvas.getContext("2d")
    private val kanjiViewer = KanjiViewer(Bounds(0, 0, 500, 500),
            context as CanvasRenderingContext2D)


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

        kanjiViewer.drawKanji(loadedKanji)
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

        console.log("Test24")

        val client = HttpClient(Js) {

        }

        console.log("Test23")

        CoroutineScope(Dispatchers.Main).launch {
            val response = client.get<String>("http://0.0.0.0:8094/kanji/27355/segment/1/matrix") {

//                contentType(ContentType.Application.Json)
//                accept(ContentType.Application.Json)
            }


            val parsedResponse = JSON.parse<Matrix<Int>>(response)



            console.log("Response: $parsedResponse")

            kanjiViewer.drawKanji(parsedResponse)

        }



    }




}