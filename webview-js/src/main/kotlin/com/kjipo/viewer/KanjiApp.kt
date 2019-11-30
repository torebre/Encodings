package com.kjipo.viewer

import KanjiViewer
import com.github.aakira.napier.Napier
import com.kjipo.representation.EncodedKanji
import com.kjipo.representation.LineUtilities
import com.kjipo.representation.Matrix
import com.kjipo.representation.SegmentLine
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


    fun loadLineSegmentData() {
        val client = HttpClient(Js)

        CoroutineScope(Dispatchers.Main).launch {
            val segmentData = client.get<String>("http://0.0.0.0:8094/kanji/27355/segmentdata")
            val parsedResponse = JSON.parse<Array<SegmentLine>>(segmentData)
            val segmentLineMap = parsedResponse.groupBy { it.segment }

            for (entry in segmentLineMap.entries) {
                val dataMatrix = LineUtilities.drawLines(entry.value)
                addCanvas(dataMatrix)
            }
        }
    }


    private fun addCanvas(matrix: Matrix<Int>) {
        try {
            val element = document.createElement("canvas") as HTMLCanvasElement

            document.body?.appendChild(element)

            val context2 = element.getContext("2d")
            val kanjiViewer2 = KanjiViewer(context2 as CanvasRenderingContext2D)

            with(element) {
                setAttribute("width", (2 * matrix.numberOfColumns).toString())
                setAttribute("height", (2 * matrix.numberOfRows).toString())
            }

            kanjiViewer2.setupKanjiDrawing(matrix, 2)
        }
        catch (e: ClientRequestException) {
            Napier.e("Could not find segment", e)
        }



    }


}