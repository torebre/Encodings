package com.kjipo

import com.kjipo.experiments.LookupSample
import com.kjipo.experiments.SearchDescription
import com.kjipo.representation.LineUtilities
import com.kjipo.representation.Matrix
import createIndexLinePrototypeMap
import getColour
import kotlinx.browser.document
import org.w3c.dom.Element
import mu.KotlinLogging

class SearchVisualization(
    numberOfRows: Int,
    numberOfColumns: Int,
    lookupSample: LookupSample,
    private val searchDescription: SearchDescription,
    parentElement: String,
) : MatrixSvg(numberOfRows, numberOfColumns, parentElement) {

    private val transformedLines: Map<Int, List<Pair<Int, Int>>>
//    private val svgElement: Element
//    private val lineMatrix: Matrix<Int>
//    private val matrixCoordinateSvgRectangleMap: Matrix<Element>

    private val logger = KotlinLogging.logger {}

    init {
//        val element = document.getElementById(parentElement)
//        svgElement = document.createElementNS(SVG_NAMESPACE_URI, "svg").also {
//            it.setAttribute("width", "200")
//            it.setAttribute("width", "200")
//        }
//        element?.appendChild(svgElement)

        transformedLines = createIndexLinePrototypeMap(lookupSample.linePrototypes)

//        drawMatrixWithNoHighlights().let {
//            lineMatrix = it.first
//            matrixCoordinateSvgRectangleMap = it.second
//        }
    }

    fun showSearch(stepId: Int) {

        logger.debug { "Test31" }

        val searchStepsToDraw = searchDescription.searchPlayThrough.filter {
            it.stepId == stepId
        }

        searchStepsToDraw.map {

            logger.debug { "Test30" }

            colourLine(it.lineAddedId)
        }

    }


//    private fun setupTranslatedElement(svgElement: Element, xShift: Int, yShift: Int): Element? {
//        return svgElement.ownerDocument?.let {
//            val groupingElement = it.createElementNS(SVG_NAMESPACE_URI, "g")
//            groupingElement.setAttribute("transform", "translate(${xShift}, ${yShift})")
//
//            svgElement.appendChild(groupingElement)
//            groupingElement
//        }
//    }

    private fun colourLine(lineId: Int, value: Int = 2, colour: String = "red") {
        transformedLines[lineId]?.let { points ->
            points.forEach {
                valueMatrix[it.first, it.second] = value
                matrixCoordinateSvgRectangleMap[it.first, it.second].setAttribute("fill", colour)
            }
        }
    }

    fun markLines(linesIds: List<Int>, value: Int, colour: String) {
        linesIds.forEach { colourLine(it, value, colour) }
    }

//    private fun drawMatrixInSvgElement(svgElement: Element, lineMatrix: Matrix<Int>): Matrix<Element> {
//        svgElement.setAttribute(
//            "viewBox",
//            "0 0 ${lineMatrix.numberOfColumns * columnShift} ${lineMatrix.numberOfRows * rowShift}"
//        )
//
//        return Matrix(lineMatrix.numberOfRows, lineMatrix.numberOfColumns) { row, column ->
//            svgElement.ownerDocument!!.let { ownerDocument ->
//                ownerDocument.createElementNS(SVG_NAMESPACE_URI, "rect").also {
//                    it.setAttribute("width", "$rectangleWidth")
//                    it.setAttribute("height", "$rectangleHeight")
//                    it.setAttribute("fill", getColour(lineMatrix[row, column]))
//                }.also { rectangle ->
//                    setupTranslatedElement(svgElement, column * columnShift, row * rowShift)?.let { translatedElement ->
//                        translatedElement.appendChild(rectangle)
//                        svgElement.appendChild(translatedElement)
//                    }
//                }
//            }
//        }
//    }


}