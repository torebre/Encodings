package com.kjipo.experiments

import com.kjipo.representation.Matrix
import com.kjipo.representation.pointsmatching.Border
import com.kjipo.representation.pointsmatching.PointsPlacer.Companion.backgroundRegion
import com.kjipo.representation.pointsmatching.PointsPlacer.Companion.interiorPointRegion
import java.nio.file.Files
import java.nio.file.Path
import java.util.*


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
