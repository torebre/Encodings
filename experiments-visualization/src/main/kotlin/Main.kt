package com.kjipo

import com.kjipo.experiments.CircleLineTest
import com.kjipo.experiments.PointsTest
import com.kjipo.experiments.loadKanjiMatrix
import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.makeSquare
import com.kjipo.representation.raster.makeThin
import java.nio.file.Path


private fun showEndpointResults() {
    val pointsTest = PointsTest()
    val visualizationData = pointsTest.setupEndpointMatching()

    ExperimentApplication.displayVisualizationData(visualizationData)
}


private fun showKanjiImage() {
    val unicode = 32769
    val imageSquareMatrix =
        makeSquare(loadKanjiMatrix(Path.of("/home/student/workspace/testEncodings/temp/kanjiOutput/$unicode.dat")))

    val updatedMatrix = Matrix(imageSquareMatrix.numberOfRows, imageSquareMatrix.numberOfColumns) { row, column ->
        imageSquareMatrix[column, row]
    }

    val imageMatrix = makeThin(updatedMatrix)

    ExperimentApplication.displayKanjiImage(listOf(updatedMatrix, imageMatrix))
}

private fun showMatrixVisualizations() {
    val circleLineTest = CircleLineTest()
    val matrixVisualization = circleLineTest.extractLineSegments(32769)

    ExperimentApplication.showMatrixVisualization(matrixVisualization)
}


fun main() {
//    showEndpointResults()
    showMatrixVisualizations()
}