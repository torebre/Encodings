package com.kjipo

import com.kjipo.experiments.LookupSample
import com.kjipo.experiments.SearchDescription
import com.kjipo.experiments.SearchPlaythroughStep
import com.kjipo.representation.LineUtilities
import kotlinx.browser.document
import org.w3c.dom.Element

class SearchVisualization(
    private val searchDescription: SearchDescription,
    lookupSamples: Collection<LookupSample>
) {

    private val SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg"


    private val lookupSampleMap = lookupSamples.associateBy { it.id }


    fun showSearch(stepId: Int, sampleId: Int) {
//       searchDescription.similarSamples

        val searchStepsToDraw = searchDescription.searchPlayThrough.filter {
            it.sampleId == sampleId && it.stepId == stepId
        }

        val svgElement = document.createElementNS(SVG_NAMESPACE_URI, "svg")
        document.appendChild(svgElement)

        lookupSampleMap[sampleId]?.let { lookupSample ->
            drawSearchStep(lookupSample, searchStepsToDraw, svgElement)
        }


    }


    private fun setupTranslatedElement(svgElement: Element, xShift: Int, yShift: Int): Element? {
        return svgElement.getOwnerDocument()?.let {
            val groupingElement = it.createElementNS(SVG_NAMESPACE_URI, "g")
            groupingElement.setAttribute("transform", "translate(${xShift}, ${yShift})")

            svgElement.appendChild(groupingElement)
            groupingElement
        }
    }

    private fun drawSearchStep(
        lookupSample: LookupSample,
        stepsToDraw: List<SearchPlaythroughStep>,
        svgElement: Element
    ) {
        val transformedLines = lookupSample.linePrototypes.map {
            LineUtilities.createLine(it.startPair.column, it.startPair.row, it.endPair.column, it.endPair.row)
        }
        val lineMatrix = LineUtilities.setupEmptyMatrix(transformedLines)
        val linesAdded = stepsToDraw.map { it.lineAddedId }

        transformedLines.mapIndexed { index, transformedLine ->
            val value = if (index in linesAdded) {
                2
            } else {
                1
            }

            LineUtilities.addLineToMatrix(transformedLine, lineMatrix, value)
        }


        val rowShift = 20
        val columnShift = 20
        lineMatrix.forEachIndexed { column, row, value ->
            svgElement.getOwnerDocument()?.let { ownerDocument ->
                val rectangle = ownerDocument.createElementNS(SVG_NAMESPACE_URI, "rect").also {
                    it.setAttribute("width", "20")
                    it.setAttribute("height", "20")
                    it.setAttribute("fill", if (value == 2) "red" else if (value == 1) "blue" else "black")
                }

                setupTranslatedElement(svgElement, column * columnShift, row * rowShift)?.let { translatedElement ->
                    translatedElement.appendChild(rectangle)
                    ownerDocument.appendChild(translatedElement)
                }
            }
        }


    }


}