package com.kjipo.experiments

import com.kjipo.representation.Matrix
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
