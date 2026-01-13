package com.kjipo.experiments

import com.kjipo.representation.LineUtilities.createLine
import com.kjipo.representation.Matrix
import com.kjipo.representation.pointsmatching.Border
import com.kjipo.representation.raster.EncodingUtilities.validCoordinates
import com.kjipo.representation.raster.FlowDirection
import com.kjipo.representation.raster.getFlowDirectionForOffset
import representation.backgroundRegion
import representation.borderClassificationStartCount
import representation.borderRegion
import representation.extractBordersInBooleanMatrix2
import representation.findBorderPoint
import representation.firstLineColor
import representation.getConnectedPoints2
import representation.interiorPointRegion


class BorderClassification {

    fun extractBorderClassification(imageMatrix: Matrix<Boolean>): List<Matrix<Int>> {
        val borders = extractBordersInBooleanMatrix2(imageMatrix)

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

                    val (lineStopRow, lineStopColumn) = getDerivativeLine(
                        it.point.first,
                        it.direction,
                        result.numberOfRows,
                        result.numberOfColumns,
                        it.point.second
                    )
                    createLine(it.point.first, it.point.second, lineStopRow, lineStopColumn).let { line ->
                        line.forEach { point ->
                            resultMatrixCopy[point.first, point.second] = firstLineColor
                        }
                    }

                    val (lineStopRow2, lineStopColumn2) = getDerivativeLine(
                        it.point.first,
                        it.secondDirection,
                        result.numberOfRows,
                        result.numberOfColumns,
                        it.point.second
                    )
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

    fun extractBorderClassification2(imageMatrix: Matrix<Boolean>): List<Matrix<Int>> {
        val borderMatrix = createInteriorAndBackgroundRegions(imageMatrix)

        addBorderRegion(borderMatrix)
        val trimmedBorderMatrixCopy = Matrix.copy(borderMatrix)
        trimBorder(trimmedBorderMatrixCopy)


        // Remove everything from image except border
//        trimmedBorderMatrixCopy.forEachIndexed { row, column, value ->
//            if (value == interiorPointRegion) {
//                trimmedBorderMatrixCopy[row, column] = backgroundRegion
//            }
//        }

//        var currentColour = borderClassificationStartCount

        val borders = extractBordersFromMatrix(imageMatrix)

//        borders.let { borders ->
//            borders.forEach { border ->
//                border.points.forEach { trimmedBorderMatrixCopy[it.first, it.second] = borderRegion }
////                ++currentColour
//            }
//        }

        val deltaPoints = mutableListOf<Pair<Pair<Int, Int>, Int>>()
        for(border in borders) {
            val pointArray: Array<Pair<Int, Int>> = Array(10) { Pair(0, 0) }
            var seenPoints = 0

            for(point in border.points) {
                pointArray[9] = point

                ++seenPoints
                if(seenPoints < 10) {
                    continue
                }

                var xDelta = 0
                var yDelta = 0
                for(index in 1 until 5) {
                    xDelta += pointArray[index].first - pointArray[index - 1].first
                    yDelta += pointArray[index].second - pointArray[index - 1].second
                }

                var xDelta2 = 0
                var yDelta2 = 0
                for(index in 5 until 10) {
                    xDelta2 += pointArray[index].first - pointArray[index - 1].first
                    yDelta2 += pointArray[index].second - pointArray[index - 1].second
                }

                if(xDelta2 - xDelta == 0 || yDelta2 - yDelta == 0) {
                    deltaPoints.add(Pair(point, 0))
                }
                else {
                    deltaPoints.add(Pair(point, (xDelta2 - xDelta) / (yDelta2 - yDelta)))
                }

                System.arraycopy(pointArray, 1, pointArray, 0, pointArray.size - 1)
                pointArray[pointArray.size - 1] = point
            }
        }

        for (pair in deltaPoints) {
            trimmedBorderMatrixCopy[pair.first.first, pair.first.second] = pair.second
        }


        // TODO Only here for testing
//        val ballRoller2 = BallRoller2()
//        val resultMatrix = ballRoller2.placeBall(trimmedBorderMatrixCopy)

        return listOf(trimmedBorderMatrixCopy)
//        return listOf(resultMatrix)
    }


    private fun extractBordersFromMatrix(imageMatrix: Matrix<Boolean>): MutableList<Border> {
        val borderMatrix = createInteriorAndBackgroundRegions(imageMatrix)

        addBorderRegion(borderMatrix)

        // Remove everything from image except border
        borderMatrix.forEachIndexed { row, column, value ->
            if (value == interiorPointRegion) {
                borderMatrix[row, column] = backgroundRegion
            }
        }

        val trimmedBorderMatrixCopy = Matrix.copy(borderMatrix)
        trimBorder(trimmedBorderMatrixCopy)

        val borders = mutableListOf<Border>()
        while (true) {
            val borderPoint = findBorderPoint(trimmedBorderMatrixCopy, borderRegion) ?: break
            val border = getConnectedPoints2(borderPoint.first, borderPoint.second, trimmedBorderMatrixCopy)
            borders.add(Border(border))

            border.forEach { trimmedBorderMatrixCopy[it.first, it.second] = backgroundRegion }
        }

        return borders
    }

    private fun createInteriorAndBackgroundRegions(imageMatrix: Matrix<Boolean>): Matrix<Int> {
        val valueExtractFunction = { row: Int, column: Int ->
            if (imageMatrix[row, column]) {
                interiorPointRegion
            } else {
                backgroundRegion
            }
        }

        val borderMatrix = Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns)
        { row, column ->
            valueExtractFunction(row, column)
        }
        return borderMatrix
    }

    private fun trimBorder(matrixCopy: Matrix<Int>) {
        matrixCopy.forEachIndexed { row, column, value ->
            if (value == borderRegion) {
                if (hasValues(
                        row,
                        column,
                        matrixCopy,
                        borderRegion,
                        FlowDirection.SOUTH,
                        FlowDirection.WEST
                    )
                    || hasValues(
                        row,
                        column,
                        matrixCopy,
                        borderRegion,
                        FlowDirection.SOUTH,
                        FlowDirection.EAST
                    )
                    || hasValues(
                        row,
                        column,
                        matrixCopy,
                        borderRegion,
                        FlowDirection.NORTH,
                        FlowDirection.WEST
                    )
                    || hasValues(
                        row,
                        column,
                        matrixCopy,
                        borderRegion,
                        FlowDirection.NORTH,
                        FlowDirection.EAST
                    )
                ) {
                    matrixCopy[row, column] = backgroundRegion
                }
            }
        }
    }

    private fun addBorderRegion(matrixToUpdate: Matrix<Int>) {
        Matrix.copy(matrixToUpdate).let { originalMatrix ->
            originalMatrix.forEachIndexed { row, column, value ->
                if (value == backgroundRegion) {
                    val neighbourhood = originalMatrix.getNeighbourhood<Int>(row, column)

                    neighbourhood.forEach { neighbour ->
                        if (neighbour != null && neighbour != backgroundRegion) {
                            matrixToUpdate[row, column] = borderRegion
                            return@forEach
                        }
                    }
                }
            }
        }
    }

    private fun <T> hasValues(
        row: Int,
        column: Int,
        borderMatrixCopy: Matrix<T>,
        valueToMatch: T,
        vararg flowDirections: FlowDirection
    ): Boolean {
        for (direction in flowDirections) {
            val neighbourRow = row + direction.rowShift
            val neighbourColumn = column + direction.columnShift

            if (!validCoordinates(
                    neighbourRow,
                    neighbourColumn,
                    borderMatrixCopy.numberOfRows,
                    borderMatrixCopy.numberOfColumns
                )
                || borderMatrixCopy[neighbourRow, neighbourColumn] != valueToMatch
            ) {
                return false
            }
        }
        return true
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


    class BorderWithAcceleration(
        val border: Border,
        val accelerations: Map<Pair<Int, Int>, Int>,
        val derivativeLines: List<DerivativeLine>
    )

    class DerivativeLine(val point: Pair<Int, Int>, val direction: FlowDirection, val secondDirection: FlowDirection)


    private fun calculateDerivative(border: Border): BorderWithAcceleration? {
        if (border.points.size < 4) {
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

            if (firstDirection != null && secondDirection != null) {
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