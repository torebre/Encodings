package com.kjipo.viewer

import KanjiViewer
import com.kjipo.representation.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.*
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel
import mu.KotlinLogging
import kotlinx.browser.document
import kotlinx.dom.clear


class KanjiApp() {
    private val client = HttpClient(Js)
    private var selectedKanji = 0

    val logger = KotlinLogging.logger {}

    init {
        KotlinLoggingConfiguration.LOG_LEVEL = KotlinLoggingLevel.DEBUG
    }

    fun setupKanjiSelection() {
        CoroutineScope(Dispatchers.Main).launch {
            val unicodes = client.get<String>("http://0.0.0.0:8094/kanji")

            document.querySelector("#kanjiUnicode")?.let {
                val selectElement = it as HTMLSelectElement

                for (unicode in unicodes.split(",")) {
                    selectElement.options.add(Option(text = unicode))
                }

                selectElement.namedItem(selectedKanji.toString())?.let { elementToSelect ->
                    selectElement.selectedIndex = elementToSelect.index
                }

                selectElement.onchange = {
                    selectElement[selectElement.selectedIndex]?.let { option ->
                        val optionElement = option as Option
                        loadLineSegmentData(optionElement.text.toInt())
                    }
                }
            }
        }
    }

    fun loadEncodedKanjiFromString(kanjiString: String, unicode: Int) =
        EncodedKanji(loadEncodedKanjiFromString(kanjiString.split('\n')), unicode)

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


    fun selectAndLoad(unicode: Int) {
        selectedKanji = unicode
        loadLineSegmentData(selectedKanji)
    }

    fun loadLineSegmentData(unicode: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val kanjiMatrix = client.get<String>("http://0.0.0.0:8094/kanji/${unicode}/matrix")
            val parsedKanjiMatrix = JSON.parse<Matrix<Int>>(kanjiMatrix)
            drawOnExistingCanvas(parsedKanjiMatrix, 3, false, "kanjiMatrix")

            val lineMatrix = tranformLinesToMatrix(unicode, client)
            drawOnExistingCanvas(lineMatrix, 3, true, "selectedKanji")

            val imageList = document.getElementById("subimages") as HTMLUListElement
            imageList.clear()

            try {
                console.log("Loading data for $unicode")

                val segmentData = client.get<String>("http://0.0.0.0:8094/kanji/${unicode}/segmentdata")
                val parsedResponse = JSON.parse<Array<SegmentLine>>(segmentData)
                val segmentLineMap = parsedResponse.groupBy { it.segment }

                for (entry in segmentLineMap.entries) {
                    val listElement = document.createElement("li")
                    imageList.appendChild(listElement)

                    val dataMatrix = LineUtilities.drawLines(entry.value)
                    addCanvas(dataMatrix, parent = listElement, useColours = true)
                }

                val segmentData2 = client.get<String>("http://0.0.0.0:8094/kanji/${unicode}/encodedsegments")
                val parsedResponse2 = JSON.parse<Matrix<Int>>(segmentData2)
                drawSquaresOnExistingCanvas(parsedResponse2, 3, "selectedKanji")

            } catch (exception: Exception) {
                logger.error(exception) {
                    exception.message.orEmpty()
                }
            }
        }
    }

    private suspend fun tranformLinesToMatrix(unicode: Int, client: HttpClient): Matrix<Int> {
        val lineData = client.get<String>("http://0.0.0.0:8094/kanji/$unicode/linedata")
        val parsedLines = JSON.parse<Array<Line>>(lineData)
        return LineUtilities.drawLines(parsedLines.toList())
    }

    private fun addCanvas(
        matrix: Matrix<Int>,
        squareSize: Int = 2,
        useColours: Boolean = false,
        parent: Element? = null
    ) {
        try {
            val element = document.createElement("canvas") as HTMLCanvasElement

            if (parent != null) {
                parent.appendChild(element)
            } else {
                document.body?.appendChild(element)
            }
            drawOnExistingCanvas(matrix, squareSize, useColours, element)
        } catch (exception: ClientRequestException) {
            logger.error(exception) {
                "Could not find segment"
            }
        }
    }

    private fun drawOnExistingCanvas(
        matrix: Matrix<Int>,
        squareSize: Int = 2,
        useColours: Boolean = false,
        canvasElementId: String
    ) {
        document.getElementById(canvasElementId)?.let {
            val context2 = (it as HTMLCanvasElement).getContext("2d")
            val kanjiViewer = KanjiViewer(context2 as CanvasRenderingContext2D)

            with(it) {
                setAttribute("width", (squareSize * matrix.numberOfColumns).toString())
                setAttribute("height", (squareSize * matrix.numberOfRows).toString())
                setAttribute("style", "border:1px solid #000000;")
            }

            kanjiViewer.setupKanjiDrawing(matrix, squareSize, useColours)
        }

    }

    private fun drawOnExistingCanvas(
        matrix: Matrix<Int>,
        squareSize: Int = 2,
        useColours: Boolean = false,
        canvasElement: Element
    ) {
        val context2 = (canvasElement as HTMLCanvasElement).getContext("2d")
        val kanjiViewer = KanjiViewer(context2 as CanvasRenderingContext2D)

        with(canvasElement) {
            setAttribute("width", (squareSize * matrix.numberOfColumns).toString())
            setAttribute("height", (squareSize * matrix.numberOfRows).toString())
            setAttribute("style", "border:1px solid #000000;")
        }
        kanjiViewer.setupKanjiDrawing(matrix, squareSize, useColours)
    }

    private fun drawSquaresOnExistingCanvas(matrix: Matrix<Int>, squareSize: Int = 2, canvasElementId: String) {
        document.getElementById(canvasElementId)?.let {
            val context2 = (it as HTMLCanvasElement).getContext("2d")
            val kanjiViewer = KanjiViewer(context2 as CanvasRenderingContext2D)

            kanjiViewer.drawSquares(matrix, squareSize)
        }
    }


}