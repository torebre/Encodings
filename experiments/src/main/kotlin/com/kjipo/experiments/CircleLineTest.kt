package com.kjipo.experiments

import com.kjipo.readetl.EtlDataReader.extractEtlImagesForUnicodeToKanjiData
import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.*
import kotlin.math.*

class CircleLineTest {

    val logger = System.getLogger(CircleLineTest::class.qualifiedName!!)


    fun extractLineSegments(unicode: Int): List<MatrixVisualization<Int>> {
//        val unicode = 32769

//        val imageMatrix =
//            makeThin(makeSquare(loadKanjiMatrix(Path.of("/home/student/workspace/testEncodings/temp/kanjiOutput/$unicode.dat"))))

//        writeOutputMatrixToPngFile(
//            imageMatrix,
//            outputDirectory.resolve("${unicode}_kanji_from_input_file.png").toFile()
//        ) { value ->
//            if (value) colourMap[0] else colourMap[1]
//        }


        // TODO Remove restriction on number of images to load
        val kanjiImages = extractEtlImagesForUnicodeToKanjiData(unicode, 5).take(1)
            .map {
                val squareMatrix = transposeMatrix(
                    makeSquare(
                        scaleMatrix(
                            transformToBooleanMatrix(
                                it.kanjiData,
                                ::simpleThreshold
                            ), 128, 128
                        )
                    )
                )

                val thinnedImage = makeThin(squareMatrix)

                val matrix2 = Matrix(squareMatrix.numberOfRows, squareMatrix.numberOfColumns) { row, column ->
                    if (squareMatrix[row, column]) {
                        if (thinnedImage[row, column]) {
                            1
                        } else {
                            2
                        }
                    } else {
                        0
                    }
                }

                placeCircles(matrix2, thinnedImage)

                var maxValue = 0
                matrix2.forEach {
                    if (it > maxValue) {
                        maxValue = it
                    }
                }

                MatrixVisualization(matrix2) { value ->
                    colourFunction(value, maxValue).let {
                        PointColor(
                            it[0].toDouble() / 255.0,
                            it[1].toDouble() / 255.0,
                            it[2].toDouble() / 255.0
                        )
                    }
                }
            }
            .toList()

        return kanjiImages
    }


    private fun placeCircles(matrixWithCenterLines: Matrix<Int>, thinnedImage: Matrix<Boolean>) {
        val matrix = Matrix.copy(matrixWithCenterLines)
        val thinnedCopy = Matrix.copy(thinnedImage)
        val circleMask = determineCircleMask(5)

        var pathCounter = 0
        var colourCounter = 3
        var circleColourCounter = 10

        for (row in 0 until matrix.numberOfRows) {
            for (column in 0 until matrix.numberOfColumns) {
                if (thinnedCopy[row, column]) {
                    val path = followCenterLine(Pair(row, column), thinnedCopy)
                    if (path != null) {
                        pathCounter++

                        path.let {
                            it.steps.forEach {
                                matrixWithCenterLines[it.first, it.second] = colourCounter
                                thinnedCopy[it.first, it.second] = false
                            }
                        }

                        path.getCenterPoint()?.let { centerPoint ->
                            applyCircleMask(
                                centerPoint.first,
                                centerPoint.second,
                                matrixWithCenterLines,
                                circleMask
                            ) { _, _ ->
                                circleColourCounter
                            }
                        }
                        circleColourCounter += 10
                    }

                    ++colourCounter


//                    val circlePoints =
//                        determineCircle(row, column, thinnedImage.numberOfRows, thinnedImage.numberOfColumns, offsets)

//                    applyCircleMask(
//                        thinnedImage.numberOfRows / 2,
//                        thinnedImage.numberOfColumns / 2,
//                        matrixWithCenterLines,
//                        circleMask
//                    )

                }

            }

        }

        logger.log(System.Logger.Level.INFO, "Number of paths: " + pathCounter)

    }


    private class Path(val startPoint: Pair<Int, Int>, val steps: MutableList<Pair<Int, Int>> = mutableListOf()) {

        fun extendPath(step: Pair<Int, Int>): Path {
            steps.add(step)

            return this
        }

        fun getLatestStep(): Pair<Int, Int> {
            if (steps.isEmpty()) {
                return startPoint
            }
            return steps.last()
        }

        fun getLength(): Double {
            return sqrt(
                (startPoint.first - steps.last().first).toDouble()
                    .pow(2) + (startPoint.second - steps.last().second).toDouble().pow(2)
            )
        }

        fun getCenterPoint(): Pair<Int, Int>? {
            if (steps.isEmpty()) {
                return null
            }

            var minRow = Int.MAX_VALUE
            var maxRow = Int.MIN_VALUE
            var minColumn = Int.MAX_VALUE
            var maxColumn = Int.MIN_VALUE

            for (step in steps) {
                if (minRow > step.first) {
                    minRow = step.first
                }
                if (maxRow < step.first) {
                    maxRow = step.first
                }
                if (minColumn > step.second) {
                    minColumn = step.second
                }
                if (maxColumn < step.second) {
                    maxColumn = step.second
                }
            }

            return Pair(minRow + (maxRow - minRow), minColumn + (maxColumn - minColumn))
        }

    }

    private fun followCenterLine(startPoint: Pair<Int, Int>, thinnedImage: Matrix<Boolean>): Path? {
        val directionsToFollow = mutableListOf<FlowDirection>()
        val paths = mutableListOf<Path>()

        val thinnedImageCopy = Matrix.copy(thinnedImage)

        thinnedImageCopy[startPoint.first, startPoint.second] = false

        val firstNeighbourhood = getNeighbourhood(thinnedImageCopy, startPoint)
        FlowDirection.entries.forEach { direction ->
            if (firstNeighbourhood[1 + direction.rowShift, 1 + direction.columnShift]) {
                directionsToFollow.add(direction)
                paths.add(
                    Path(startPoint).extendPath(
                        Pair(
                            startPoint.first + direction.rowShift,
                            startPoint.second + direction.columnShift
                        )
                    )
                )
                thinnedImageCopy[startPoint.first + direction.rowShift, startPoint.second + direction.columnShift] =
                    false
            }
        }

        var pathsExtended = true
        while (pathsExtended) {
            pathsExtended = false

            for (path1 in paths) {
                val neighbourhood = getNeighbourhood(thinnedImageCopy, path1.getLatestStep())
                FlowDirection.entries.forEach { direction ->
                    if (neighbourhood[1 + direction.rowShift, 1 + direction.columnShift]) {
                        directionsToFollow.add(direction)

                        thinnedImageCopy[path1.getLatestStep().first + direction.rowShift,
                            path1.getLatestStep().second + direction.columnShift] = false

                        path1.extendPath(
                            Pair(
                                path1.getLatestStep().first + direction.rowShift,
                                path1.getLatestStep().second + direction.columnShift
                            )
                        )
                        pathsExtended = true
                    }
                }

                if (path1.getLength() > 2 * CIRCLE_RADIUS) {
                    return path1
                }
            }
        }

        // TODO
        if (paths.isEmpty()) {
            return null
        } else {
            return paths.first()
        }

//        return null
    }


    private fun determineCircle(
        row: Int,
        column: Int,
        maxRow: Int,
        maxColumn: Int,
        circleOffsets: Collection<Pair<Int, Int>>
    ): List<Pair<Int, Int>> {
        return circleOffsets.filter {
            row + it.first >= 0
                    && column + it.second >= 0
                    && row + it.first < maxRow
                    && column + it.second < maxColumn
        }.toList()
    }


    companion object {
        const val CIRCLE_RADIUS = 10

    }


}


fun main() {
    val circleLineTest = CircleLineTest()

    circleLineTest.extractLineSegments(32769)


}
