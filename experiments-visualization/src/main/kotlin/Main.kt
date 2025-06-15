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

//    val kanjiImage = extractEtlImagesForUnicodeToKanjiData(32769, 5).take(1)
    val kanjiImage = extractEtlImagesForUnicodeToKanjiData(34152, 5).take(1)
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
    val kanjiColor = PointColor(0.0, 0.0, 1.0)

    val matrixToVisualize = Matrix(kanjiImage.numberOfRows, kanjiImage.numberOfColumns) { row, column ->
        if (midpointImage[row, column]) {
            1
        } else if (kanjiImage[row, column]) {
            2
        } else {
            0
        }

    }

//    ExperimentApplication.showMatrixVisualization(MatrixVisualization(midpointImage, { value ->
//        if (value) {
//            midpointColor
//        } else {
//            backgroundColor
//        }
//    }))

    ExperimentApplication.showMatrixVisualization(MatrixVisualization(matrixToVisualize, { value ->
        when (value) {
            1 -> midpointColor
            2 -> kanjiColor
            else -> backgroundColor
        }
    }))

}


private fun extractStrokes() {
    val ballRoller = BallRoller()

//    val kanjiImage = extractEtlImagesForUnicodeToKanjiData(32769, 5).take(1)
    val kanjiImage = extractEtlImagesForUnicodeToKanjiData(34152, 5).take(1)
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

    val paths = ballRoller.createPathFromCircle(kanjiImage)
    val updatedImage = Matrix(kanjiImage.numberOfRows, kanjiImage.numberOfColumns, { row, column ->
        if (kanjiImage[row, column]) {
            1
        } else {
            0
        }
    })

    val numberOfColours = paths.size + 1

//    var counter = 0
//    for (path in paths) {
//        for (circlePathStep in path.path) {
//            applyCircleMask(
//                circlePathStep.circleCenter.row,
//                circlePathStep.circleCenter.column,
//                updatedImage,
//                circlePathStep.circleMaskInformation.circleMask,
//                { row, column ->
//                    counter + 2
//                })
//        }
//        ++counter
//    }

    ballRoller.drawPathsOnImage(updatedImage, paths)

//    ExperimentApplication.showMatrixVisualization(MatrixVisualization(updatedImage, { value ->
//        if (value == 0) {
//            PointColor(0.0, 0.0, 0.0)
//        } else {
//            colourFunction(value, numberOfColours).let { colourArray ->
//                PointColor(
//                    colourArray[0].div(255.0),
//                    colourArray[1].div(255.0),
//                    colourArray[2].div(255.0)
//                )
//            }
//        }
//
//    }
//    ))

    val colors = generateEvenlyDistributedColors2(paths.size + 2)
    ExperimentApplication.showMatrixVisualization(MatrixVisualization(updatedImage, { value ->
        if (value == 0) {
            PointColor(0.0, 0.0, 0.0)
        } else {
            colors[value]
        }
    }
    ))


//    val colors = generateEvenlyDistributedColors2(paths.size + 2)
//    val colorMatrices = ballRoller.matricesToDisplay.map { matrix ->
//        Matrix(matrix.numberOfRows, matrix.numberOfColumns, { row, column ->
//            val pointColor = colors[matrix[row, column]]
//            Color.color(pointColor.red, pointColor.green, pointColor.blue)
//        })
//    }.toList()
//
//    AnimationApplication.startAnimation(colorMatrices.first(), { matrixToDisplay, counter ->
//        if (counter >= colorMatrices.size) {
//            false
//        } else {
//            for (row in 0 until matrixToDisplay.numberOfRows) {
//                for (column in 0 until matrixToDisplay.numberOfColumns) {
//                    matrixToDisplay[row, column] = colorMatrices[counter][row, column]
//                }
//            }
//            true
//        }
//    })

}


private fun extractStrokes2() {
    val ballRoller = BallRoller()

//    val kanjiImage = extractEtlImagesForUnicodeToKanjiData(32769, 5).take(1)
    val kanjiImage = extractEtlImagesForUnicodeToKanjiData(34152, 5).take(1)
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

    val paths = ballRoller.createPathFromCircle(kanjiImage)
    val extractPathsFromAreas = ExtractPathsFromAreas(paths, kanjiImage)
    val pathImage = extractPathsFromAreas.createPathImage()

    val colors = generateEvenlyDistributedColors2(getMaxValue(pathImage) + 1)
    ExperimentApplication.showMatrixVisualization(MatrixVisualization(pathImage, { value ->
        if (value == 0) {
            PointColor(0.0, 0.0, 0.0)
        } else {
            colors[value]
        }
    }
    ))
}


private fun runAnimationApplication() {
    AnimationApplication.startAnimation()

}


fun main() {
    // showEndpointResults()
    // showMatrixVisualizations()
//     findMidpoints()
//    extractStrokes()
    extractStrokes2()

//    runAnimationApplication()
}