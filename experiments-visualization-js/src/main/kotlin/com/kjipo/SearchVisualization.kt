package com.kjipo

import com.kjipo.experiments.LookupSample
import com.kjipo.experiments.SearchDescription
import createIndexLinePrototypeMap
import getColour
import mu.KotlinLogging

class SearchVisualization(
    numberOfRows: Int,
    numberOfColumns: Int,
    private val lookupSample: LookupSample,
    private val searchDescription: SearchDescription,
    parentElement: String,
    id: String
) {
    private val svgMatrix: MatrixSvg
    private val valueMatrix: ValueMatrix
    private val transformedLines: Map<Int, List<Pair<Int, Int>>>

    private val logger = KotlinLogging.logger {}

    init {
        transformedLines = createIndexLinePrototypeMap(lookupSample.linePrototypes)
        valueMatrix = ValueMatrix(numberOfRows, numberOfColumns)
        transformedLines.keys.forEach { colourLine(it) }

        svgMatrix = MatrixSvg(numberOfRows, numberOfColumns, parentElement, id) { row, column ->
            valueMatrix.getColourForCoordinate(
                row,
                column
            )
        }
    }

    fun showSearch(stepId: Int) {
        val linesToColour = searchDescription.searchPlayThrough.filter {
            it.sampleId == lookupSample.id && it.stepId == stepId
        }.map { it.lineAddedId }

        colourLineCollection(linesToColour)
    }

    private fun colourLineCollection(
        lineIds: Collection<Int>, value: Int = 3, colour: String = getColour(3),
        valueForLineNotInCollection: Int = 2, colourForLineNotInCollection: String = getColour(2)
    ) {
        transformedLines.keys.forEach {
            if (lineIds.contains(it)) {
                colourLine(it, value, colour)
            } else {
                colourLine(it, valueForLineNotInCollection, colourForLineNotInCollection)
            }
        }
        svgMatrix.refreshSvgMatrix()
    }

    private fun colourLine(lineId: Int, value: Int = 2, colour: String = getColour(2)) {
        transformedLines[lineId]?.let { points ->
            points.forEach {
                valueMatrix.valueMatrix[it.first, it.second] = value
//                matrixCoordinateSvgRectangleMap[it.first, it.second]?.setAttribute("fill", colour)
            }
        }
    }

    fun markLines(linesIds: Collection<Int>, value: Int, colour: String) {
        colourLineCollection(linesIds, value, colour)
//        linesIds.forEach { colourLine(it, value, colour) }
        svgMatrix.refreshSvgMatrix()
    }

}