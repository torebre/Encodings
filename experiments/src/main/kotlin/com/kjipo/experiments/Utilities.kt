package com.kjipo.experiments

import com.kjipo.experiments.CircleLineTest.Companion.CIRCLE_RADIUS
import com.kjipo.representation.Matrix
import representation.backgroundRegion
import representation.interiorPointRegion
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


fun loadKanjiMatrix(path: Path): Matrix<Boolean> {
    val bytesInFile = Files.readAllBytes(path)
    val bytes = Base64.getDecoder().decode(bytesInFile)
    val bitSet = BitSet.valueOf(bytes)
    val numberOfRows = bytes[0].toUByte().toInt()
    val numberOfColumns = bytes[1].toUByte().toInt()

    return Matrix(numberOfRows, numberOfColumns) { row, column ->
        bitSet[16 + row * numberOfRows + column]
    }
}

inline fun <reified T> transposeMatrix(matrix: Matrix<T>): Matrix<T> {
    return Matrix<T>(matrix.numberOfRows, matrix.numberOfColumns) { row, column ->
        matrix[column, row]
    }
}

fun simpleThreshold(rgbValue: Int): Boolean {
    if (rgbValue != -1) {
        // https://stackoverflow.com/questions/49676862/srgb-to-rgb-color-conversion
        val blue: Int = rgbValue and 255
        val green: Int = rgbValue shr 8 and 255
        val red: Int = rgbValue shr 16 and 255

        return red > 20 || green > 20 || blue > 20
    }
    return false
}


fun extractBorders(valueMatrix: Matrix<Boolean>): List<Pair<Int, Int>> {
    val borderMatrix = Matrix(valueMatrix.numberOfRows, valueMatrix.numberOfColumns)
    { row, column ->
        if (!valueMatrix[row, column]) {
            backgroundRegion
        } else {
            interiorPointRegion
        }
    }

    valueMatrix.forEachIndexed { row, column, value ->
        val neighbourhood = valueMatrix.getNeighbourhood<Boolean?>(row, column)
        var surroundedByEqualValues = true
        neighbourhood.forEach {
            if (it != null && it != value) {
                surroundedByEqualValues = false
                return@forEach
            }
        }

        if (surroundedByEqualValues) {
            borderMatrix[row, column] = backgroundRegion
        }
    }

    val borders = mutableListOf<Pair<Int, Int>>()
    while (true) {
        val borderPoint = findBorderPoint(borderMatrix, interiorPointRegion) ?: break

        borders.add(borderPoint)
        borderMatrix[borderPoint.first, borderPoint.second] = backgroundRegion
    }

    return borders
}


private fun findBorderPoint(borderMatrix: Matrix<Int>, borderValue: Int): Pair<Int, Int>? {
    borderMatrix.forEachIndexed { row, column, _ ->
        if (borderMatrix[row, column] == borderValue) {
            return Pair(row, column)
        }
    }
    return null
}

private fun getOffsetsForCircle(circleRadius: Int): Collection<Pair<Int, Int>> {
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


fun determineCircleMask(radius: Int = CIRCLE_RADIUS): Matrix<Boolean> {
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

fun extractBordersUntilNoneLeft(matrix: Matrix<Boolean>): List<List<Pair<Int, Int>>> {
    val copyMatrix = Matrix.copy(matrix)
    var border = extractBorders(copyMatrix)
    val borders = mutableListOf(border)

    do {
        border.forEach {
            copyMatrix[it.first, it.second] = false
        }

        border = extractBorders(copyMatrix)
        if (border.isNotEmpty()) {
            borders.add(border)
        }
    } while (border.isNotEmpty())

    return borders
}


fun generateGradientImage(matrix: Matrix<Boolean>): Matrix<Int> {
    val borderLists = extractBordersUntilNoneLeft(matrix)
    var distanceFromRegionEdge = 0

    val borderImage = Matrix(matrix.numberOfRows, matrix.numberOfColumns, {_, _ -> 0})
    for (borderList in borderLists) {
        borderList.forEach { borderImage[it.first, it.second] = distanceFromRegionEdge }
        ++distanceFromRegionEdge
    }

    return borderImage
}