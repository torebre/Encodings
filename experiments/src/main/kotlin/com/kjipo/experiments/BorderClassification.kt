package com.kjipo.experiments

import com.kjipo.representation.LineUtilities.createLine
import com.kjipo.representation.Matrix
import com.kjipo.representation.pointsmatching.Border
import com.kjipo.representation.raster.FlowDirection
import com.kjipo.representation.raster.getFlowDirectionForOffset
import representation.backgroundRegion
import representation.borderClassificationStartCount
import representation.extractBordersInBooleanMatrix
import representation.firstLineColor
import representation.interiorPointRegion


class BorderClassification {

    fun extractBorderClassification(imageMatrix: Matrix<Boolean>): List<Matrix<Int>> {
        val borders = extractBordersInBooleanMatrix(imageMatrix)

        val matricesToVisualize = mutableListOf<Matrix<Int>>()

        val result = Matrix(
            imageMatrix.numberOfRows, imageMatrix.numberOfColumns,
            { row, column ->
                if (imageMatrix[row, column]) {
                    interiorPointRegion
                } else {
                    backgroundRegion
                }
            })

        matricesToVisualize.add(result)

//        for (border in borders) {
//            for (pair in border.points) {
//                result[pair.first, pair.second] = borderRegion
//            }
//        }

        for (border in borders) {
            calculateDerivative(border)?.let { borderWithAcceleration ->

                borderWithAcceleration.accelerations.forEach { (point, value) ->
                    result[point.first, point.second] = borderClassificationStartCount + value
                }


                borderWithAcceleration.derivativeLines.forEach {
                    val resultMatrixCopy = Matrix.copy(result)

                    val (lineStopRow, lineStopColumn) = getDerivativeLine(it.point.first, it.direction, result.numberOfRows, result.numberOfColumns, it.point.second)
                    createLine(it.point.first, it.point.second, lineStopRow, lineStopColumn).let { line ->
                        line.forEach { point ->
                            resultMatrixCopy[point.first, point.second] = firstLineColor
                        }
                    }

                    val (lineStopRow2, lineStopColumn2) = getDerivativeLine(it.point.first, it.secondDirection, result.numberOfRows, result.numberOfColumns, it.point.second)
                    createLine(it.point.first, it.point.second, lineStopRow, lineStopColumn).let { line ->
                        line.forEach { point ->
                            resultMatrixCopy[point.first, point.second] = firstLineColor
                        }
                    }

                    matricesToVisualize.add(resultMatrixCopy)
                }
            }
        }

        return matricesToVisualize
    }

    private fun getDerivativeLine(
        lineStopRow: Int,
        direction: FlowDirection,
        numberOfRows: Int,
        numberOfColumns: Int,
        lineStopColumn: Int
    ): Pair<Int, Int> {
        var lineStopRow1 = lineStopRow
        var lineStopColumn1 = lineStopColumn
        for (i in 0 until 5) {
            val tempStopRow = lineStopRow1 + direction.rowShift
            if (tempStopRow >= 0 && tempStopRow < numberOfRows) {
                lineStopRow1 += direction.rowShift
            }

            val tempStopColumn = lineStopColumn1 + direction.columnShift
            if (tempStopColumn >= 0 && tempStopColumn < numberOfColumns) {
                lineStopColumn1 += direction.columnShift
            }
        }
        return Pair(lineStopColumn1, lineStopRow1)
    }


    class BorderWithAcceleration(val border: Border,
                                 val accelerations: Map<Pair<Int, Int>, Int>,
        val derivativeLines: List<DerivativeLine>)

    class DerivativeLine(val point: Pair<Int, Int>, val direction: FlowDirection, val secondDirection: FlowDirection)



    private fun calculateDerivative(border: Border): BorderWithAcceleration? {
        if(border.points.size < 4) {
            return null
        }

        val result = mutableMapOf<Pair<Int, Int>, Int>()
        val pointBuffer = Array(4, { Pair(0, 0) })

        border.points.take(4).forEachIndexed { index, pair ->
            pointBuffer[index] = pair
        }

        val derivativeLines = mutableListOf<DerivativeLine>()

        border.points.stream().skip(4).forEach { pair ->
            val firstDiff = pointBuffer[1].first - pointBuffer[0].first
            val secondDiff = pointBuffer[1].second - pointBuffer[0].second

            val firstDiff2 = pointBuffer[3].first - pointBuffer[2].first
            val secondDiff2 = pointBuffer[3].second - pointBuffer[2].second

            val firstDirection = getFlowDirectionForOffset(firstDiff, secondDiff)
            val secondDirection = getFlowDirectionForOffset(firstDiff2, secondDiff2)

            if(firstDirection != null && secondDirection != null) {
                val change = secondDirection.ordinal - firstDirection.ordinal
                result[pointBuffer[2]] = change

                derivativeLines.add(DerivativeLine(pointBuffer[2], firstDirection, secondDirection))
            }

            pointBuffer[0] = pointBuffer[1]
            pointBuffer[1] = pointBuffer[2]
            pointBuffer[2] = pointBuffer[3]
            pointBuffer[3] = pair
        }

        return BorderWithAcceleration(border, result, derivativeLines)
    }


}