package com.kjipo

import com.kjipo.representation.Matrix
import kotlinx.browser.document
import mu.KotlinLogging
import org.w3c.dom.Element


class MatrixSvg(
    numberOfRows: Int, numberOfColumns: Int, parentElement: String, id: String,
    private val colourProvider: (Int, Int) -> String?
) {
    private val svgElement: Element
    private val matrixCoordinateSvgRectangleMap: Matrix<Element?>

    private val logger = KotlinLogging.logger {}


    init {
        val element = document.getElementById(parentElement)!!
        svgElement = document.createElementNS(SVG_NAMESPACE_URI, "svg").also {
            it.id = id
            it.setAttribute("class", "kanji-svg")
            it.setAttribute("width", "${numberOfColumns * rectangleWidth}")
            it.setAttribute("height", "${numberOfRows * rectangleHeight}")
        }
        element.appendChild(svgElement)

        svgElement.setAttribute(
            "viewBox",
            "0 0 ${numberOfColumns * rectangleWidth} ${numberOfRows * rectangleHeight}"
        )
        matrixCoordinateSvgRectangleMap =
            drawMatrixInSvgElement(svgElement, numberOfRows, numberOfColumns, colourProvider)
    }


    private fun setupTranslatedElement(svgElement: Element, xShift: Int, yShift: Int): Element? {
        return svgElement.ownerDocument?.let {
            val groupingElement = it.createElementNS(SVG_NAMESPACE_URI, "g")
            groupingElement.setAttribute("transform", "translate(${xShift}, ${yShift})")

            svgElement.appendChild(groupingElement)
            groupingElement
        }
    }

    fun refreshSvgMatrix() {
        logger.info { "Test80" }
        var counter = 0
        val colours = mutableSetOf<String>()
        matrixCoordinateSvgRectangleMap.forEachIndexed { row, column, element ->
            // TODO Could also handle cases where the element is null
            if (element != null) {
                val colour = colourProvider(row, column)
                if (colour != null) {
                    element.setAttribute("fill", colour)

                    colours.add(colour)
                    ++counter
                } else {
                    element.removeAttribute("fill")
                }
            }
        }

        logger.info { "Test81: $counter. Colours: " }
    }

    private fun drawMatrixInSvgElement(
        svgElement: Element,
        numberOfRows: Int,
        numberOfColumns: Int,
        colourProvider: (Int, Int) -> String?
    ): Matrix<Element?> {
        return Matrix(numberOfRows, numberOfColumns) { row, column ->
            val colour = colourProvider(row, column)
            if (colour != null) {
                svgElement.ownerDocument!!.let { ownerDocument ->
                    ownerDocument.createElementNS(SVG_NAMESPACE_URI, "rect").also {
                        it.setAttribute("width", "$rectangleWidth")
                        it.setAttribute("height", "$rectangleHeight")
                        it.setAttribute("fill", colour)
                    }.also { rectangle ->
                        setupTranslatedElement(
                            svgElement,
                            column * columnShift,
                            row * rowShift
                        )?.let { translatedElement ->
                            translatedElement.appendChild(rectangle)
                            svgElement.appendChild(translatedElement)
                        }
                    }
                }
            } else {
                null
            }
        }
    }
}