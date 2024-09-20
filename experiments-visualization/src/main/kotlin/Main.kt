package com.kjipo

import com.kjipo.experiments.*
import com.kjipo.readetl.EtlDataReader.extractEtlImagesForUnicodeToKanjiData
import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.makeSquare
import com.kjipo.representation.raster.makeThin
import com.kjipo.representation.raster.scaleMatrix
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


private fun findMidpoints() {
    val midpointTest = MidpointTest()

    val kanjiImage = extractEtlImagesForUnicodeToKanjiData(32769, 5).take(1)
        .map {
            transposeMatrix(
                makeSquare(
                    scaleMatrix(
                        transformToBooleanMatrix(
                            it.kanjiData,
                            ::simpleThreshold
                        ), 128, 128
                    )
                )
            )
        }.first()

    val midpointImage = midpointTest.findMidpointsInRegions(kanjiImage)

    val backgroundColor = PointColor(0.0, 0.0, 0.0)
    val midpointColor = PointColor(1.0, 1.0, 1.0)

    ExperimentApplication.showMatrixVisualization(MatrixVisualization(midpointImage, { value ->
        if (value) {
            midpointColor
        } else {
            backgroundColor
        }
    }))

}


fun main() {
    // showEndpointResults()
    // showMatrixVisualizations()
    findMidpoints()
}