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

    private val logger = KotlinLogging.logger {}

    init {
        transformedLines = createIndexLinePrototypeMap(lookupSample.linePrototypes)
    }

    fun showSearch(stepId: Int) {
        val searchStepsToDraw = searchDescription.searchPlayThrough.filter {
            it.stepId == stepId
        }

        searchStepsToDraw.map {
            colourLine(it.lineAddedId)
        }

    }


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

}