package com.kjipo

import com.kjipo.representation.LineUtilities
import com.kjipo.representation.Matrix
import kotlinx.browser.document
import mu.KotlinLogging
import org.w3c.dom.Element

open class MatrixSvg(numberOfRows: Int, numberOfColumns: Int, parentElement: String) {
    private val svgElement: Element
    protected val valueMatrix: Matrix<Int>
    protected val matrixCoordinateSvgRectangleMap: Matrix<Element>

    private val logger = KotlinLogging.logger {}


    init {
        val element = document.getElementById(parentElement)
        svgElement = document.createElementNS(SVG_NAMESPACE_URI, "svg").also {
            it.setAttribute("width", "200")
            it.setAttribute("width", "200")
        }
        element!!.appendChild(svgElement)

//        transformedLines = lookupSample.linePrototypes.mapIndexed { index, linePrototype ->
//            Pair(
//                index,
//                LineUtilities.createLine(
//                    linePrototype.startPair.column,
//                    linePrototype.startPair.row,
//                    linePrototype.endPair.column,
//                    linePrototype.endPair.row
//                )
//            )
//        }.toMap()

        valueMatrix = Matrix(numberOfColumns, numberOfRows) { _, _ -> 0 }

        svgElement.setAttribute(
            "viewBox",
            "0 0 ${numberOfColumns * rectangleWidth} ${numberOfRows * rectangleHeight}"
        )
        matrixCoordinateSvgRectangleMap =
            drawMatrixInSvgElement(svgElement, numberOfRows, numberOfColumns) { _, _ ->
                "black"
            }
    }


//    private fun drawMatrixWithNoHighlights(): Pair<Matrix<Int>, Matrix<Element>> {
//        val lineMatrix = LineUtilities.setupEmptyMatrix(transformedLines.values)
//
//        transformedLines.forEach { entry ->
//            LineUtilities.addLineToMatrix(entry.value, lineMatrix, 1)
//        }
//
//        logger.debug { "Number of lines to draw: ${transformedLines.size}" }
////        logger.debug { lineMatrix.toString() }
//
//        return Pair(lineMatrix, drawMatrixInSvgElement(svgElement, lineMatrix))
//    }


    private fun setupTranslatedElement(svgElement: Element, xShift: Int, yShift: Int): Element? {
        return svgElement.ownerDocument?.let {
            val groupingElement = it.createElementNS(SVG_NAMESPACE_URI, "g")
            groupingElement.setAttribute("transform", "translate(${xShift}, ${yShift})")

            svgElement.appendChild(groupingElement)
            groupingElement
        }
    }


    private fun drawMatrixInSvgElement(
        svgElement: Element,
        numberOfRows: Int,
        numberOfColumns: Int,
        colourProvider: (Int, Int) -> String
    ): Matrix<Element> {
        return Matrix(numberOfRows, numberOfColumns) { row, column ->
            svgElement.ownerDocument!!.let { ownerDocument ->
                ownerDocument.createElementNS(SVG_NAMESPACE_URI, "rect").also {
                    it.setAttribute("width", "$rectangleWidth")
                    it.setAttribute("height", "$rectangleHeight")
                    it.setAttribute("fill", colourProvider(row, column))
                }.also { rectangle ->
                    setupTranslatedElement(svgElement, column * columnShift, row * rowShift)?.let { translatedElement ->
                        translatedElement.appendChild(rectangle)
                        svgElement.appendChild(translatedElement)
                    }
                }
            }
        }
    }


//    protected fun getColour(value: Int): String {
//        return when (value) {
//            2 -> {
//                "red"
//            }
//
//            1 -> {
//                "blue"
//            }
//
//            else -> {
//                "black"
//            }
//        }
//    }


}