package com.kjipo

import com.kjipo.experiments.LookupSample
import createIndexLinePrototypeMap
import getColour

class InputSampleVisualization(
    numberOfRows: Int,
    numberOfColumns: Int,
    parentElement: String,
    sample: LookupSample
) : MatrixSvg(numberOfRows, numberOfColumns, parentElement) {
    private val transformedLines: Map<Int, List<Pair<Int, Int>>>


    init {
        transformedLines = createIndexLinePrototypeMap(sample.linePrototypes)
        transformedLines.keys.forEach { colourLine(it) }
    }


    private fun colourLine(lineId: Int, value: Int = 2, colour: String = "red") {
        transformedLines[lineId]?.let { points ->
            points.forEach {
                valueMatrix[it.first, it.second] = value
                matrixCoordinateSvgRectangleMap[it.first, it.second].setAttribute("fill", colour)
            }
        }
    }


    fun colourLineCollection(
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
    }

    fun markLines(linesIds: List<Int>, value: Int, colour: String) {
        linesIds.forEach { colourLine(it, value, colour) }
    }


    fun showHighlightLinesInCollection(lineIds: List<Int>) {
        transformedLines.keys.forEach {
            if (lineIds.contains(it)) {
                colourLine(it, 2, getColour(2))
            } else {
                colourLine(it, 3, getColour(3))
            }
        }
    }

}