package com.kjipo

import com.kjipo.experiments.loadKanjiMatrix
import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.makeSquare
import com.kjipo.representation.raster.makeThin
import java.nio.file.Path

fun main() {
    val unicode = 32769
    val imageSquareMatrix = makeSquare(loadKanjiMatrix(Path.of("/home/student/workspace/testEncodings/temp/kanjiOutput/$unicode.dat")))

    val updatedMatrix = Matrix(imageSquareMatrix.numberOfRows, imageSquareMatrix.numberOfColumns, { row, column ->
        imageSquareMatrix[column, row]
    })

    val imageMatrix = makeThin(updatedMatrix)

    displayKanjiImage(listOf(updatedMatrix, imageMatrix))

}