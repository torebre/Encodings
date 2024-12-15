package com.kjipo.experiments

import com.kjipo.representation.Matrix
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


fun determineCircleMask(radius: Int = CircleLineTest.CIRCLE_RADIUS): Matrix<Boolean> {
    val offsets = getOffsetsForCircle(radius)

    val maskMatrix = Matrix(2 * radius + 1, 2 * radius + 1) { _, _ ->
        true
    }

    val centerRow = maskMatrix.numberOfRows / 2
    val centerColumn = maskMatrix.numberOfColumns / 2

    offsets.forEach {
        maskMatrix[it.first + centerRow, it.second + centerColumn] = false
    }

    for (row in 0 until maskMatrix.numberOfRows) {
        for (column in 0 until maskMatrix.numberOfColumns) {
            if (!maskMatrix[row, column]) {
                break
            }
            maskMatrix[row, column] = false
        }
    }

    for (row in maskMatrix.numberOfRows - 1 downTo 0) {
        for (column in maskMatrix.numberOfColumns - 1 downTo 0) {
            if (!maskMatrix[row, column]) {
                break
            }
            maskMatrix[row, column] = false
        }
    }

//        for (row in 0 until maskMatrix.numberOfRows) {
//            var inCircle = false
//            for (column in 0 until maskMatrix.numberOfColumns) {
//                if (maskMatrix[row, column] && !inCircle) {
//                    maskMatrix[row, column] = true
//                    inCircle = true
//                }
//                else if(!maskMatrix[row, column] && inCircle) {
//                    maskMatrix[row, column] = true
//                }
//                else if (maskMatrix[row, column] && inCircle) {
//                    maskMatrix[row, column] = true
//                    inCircle = false
//                }
//                else if (!maskMatrix[row, column] && !inCircle) {
//                    maskMatrix[row, column] = false
//                }
//            }
//        }

    offsets.forEach {
        maskMatrix[it.first + centerRow, it.second + centerColumn] = true
    }

    return maskMatrix
}

fun getOffsetsForCircle(circleRadius: Int): Collection<Pair<Int, Int>> {
    val slice = 2 * Math.PI / 500

    val offsets = (0..500).map {
        val angle = it * slice
        val rowOffset = sin(angle) * circleRadius
        val columnOffset = cos(angle) * circleRadius

        Pair(rowOffset, columnOffset)
    }.toList()

    return offsets.map {
        Pair(it.first.roundToInt(), it.second.roundToInt())
    }.toSet()
}



fun <T> applyCircleMask(
    row: Int,
    column: Int,
    matrix: Matrix<T>,
    circleMask: Matrix<Boolean>,
    valueFunction: (row: Int, column: Int) -> T
) {
    val rowCenter = circleMask.numberOfRows / 2
    val columnCenter = circleMask.numberOfColumns / 2

    val upperLeftRow = row - rowCenter
    val upperLeftColumn = column - columnCenter

    circleMask.forEachIndexed { rowMask, columnMask, value ->
        if (circleMask[rowMask, columnMask]) {
            val rowInMatrix = rowMask + upperLeftRow
            val columnInMatrix = columnMask + upperLeftColumn

            if (rowInMatrix >= 0
                && rowInMatrix < matrix.numberOfRows
                && columnInMatrix >= 0
                && columnInMatrix < matrix.numberOfColumns
            ) {
                matrix[rowInMatrix, columnInMatrix] = valueFunction(rowInMatrix, columnInMatrix)
            }
        }
    }

}


