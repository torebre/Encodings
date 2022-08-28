package com.kjipo

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
        val element = document.getElementById(parentElement)!!
        svgElement = document.createElementNS(SVG_NAMESPACE_URI, "svg").also {
            it.setAttribute("width", "${numberOfColumns * rectangleWidth}")
            it.setAttribute("height", "${numberOfRows * rectangleHeight}")
        }
        element.appendChild(svgElement)
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
}