package com.kjipo

import com.kjipo.datageneration.LinePrototypeWithAngle
import createIndexLinePrototypeMap
import getColour

class InputSampleVisualization(
    numberOfRows: Int,
    numberOfColumns: Int,
    parentElement: String,
    sample: List<LinePrototypeWithAngle>
) : MatrixSvg(numberOfRows, numberOfColumns, parentElement) {
    private val transformedLines: Map<Int, List<Pair<Int, Int>>>


    init {
        transformedLines = createIndexLinePrototypeMap(sample)
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

    fun markLines(linesIds: List<Int>, value: Int, colour: String) {
        linesIds.forEach { colourLine(it, value, colour) }
    }

}