package com.kjipo.viewer

import KanjiViewer
import com.github.aakira.napier.Napier
import com.kjipo.representation.*
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

    fun loadLineSegmentData() {
        val client = HttpClient(Js)

        CoroutineScope(Dispatchers.Main).launch {
            val kanjiMatrix = client.get<String>("http://0.0.0.0:8094/kanji/27355/matrix")
            val parsedKanjiMatrix = JSON.parse<Matrix<Int>>(kanjiMatrix)

//            val encodedKanji = loadEncodedKanjiFromString(kanjiMatrix, 27355)
//            val parsedKanjiMatrix = JSON.parse<Matrix<Int>>(kanjiMatrix)

            addCanvas(parsedKanjiMatrix, 3)

            drawLines(33760, client)

            val segmentData = client.get<String>("http://0.0.0.0:8094/kanji/27355/segmentdata")
            val parsedResponse = JSON.parse<Array<SegmentLine>>(segmentData)
            val segmentLineMap = parsedResponse.groupBy { it.segment }

            for (entry in segmentLineMap.entries) {
                val dataMatrix = LineUtilities.drawLines(entry.value)
                addCanvas(dataMatrix)
            }
        }
    }

    private suspend fun drawLines(unicode: Int, client: HttpClient) {
        val lineData = client.get<String>("http://0.0.0.0:8094/kanji/$unicode/linedata")
        val parsedLines = JSON.parse<Array<Line>>(lineData)
        val matrix = LineUtilities.drawLines(parsedLines.toList())

        addCanvas(matrix, 3, true)
    }

    private fun addCanvas(matrix: Matrix<Int>, squareSize: Int = 2, useColours: Boolean = false) {
        try {
            val element = document.createElement("canvas") as HTMLCanvasElement

            document.body?.appendChild(element)

            val context2 = element.getContext("2d")
            val kanjiViewer2 = KanjiViewer(context2 as CanvasRenderingContext2D)

            with(element) {
                setAttribute("width", (squareSize * matrix.numberOfColumns).toString())
                setAttribute("height", (squareSize * matrix.numberOfRows).toString())
                setAttribute("style", "border:1px solid #000000;")
            }

            kanjiViewer2.setupKanjiDrawing(matrix, squareSize, useColours)
        } catch (e: ClientRequestException) {
            Napier.e("Could not find segment", e)
        }
    }


}