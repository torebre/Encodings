package com.kjipo.experiments

import com.kjipo.readetl.EtlDataReader.extractEtlImagesForUnicodeToKanjiData
import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.*
import kotlin.math.*


class StrokeTest {

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

    private fun findFilledPoint(image: Matrix<Boolean>): Pair<Int, Int>? {
        for(row in 0 until image.numberOfRows) {
            for(column in 0 until image.numberOfColumns) {
                if(image[row, column]) {
                    return Pair(row, column)
                }
            }
        }
        return null
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
                            ) {
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


    private fun applyCircleMask(
        row: Int,
        column: Int,
        matrix: Matrix<Int>,
        circleMask: Matrix<Boolean>,
        valueFunction: (Pair<Int, Int>) -> Int
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
                    matrix[rowInMatrix, columnInMatrix] = valueFunction(Pair(rowInMatrix, columnInMatrix))
                }
            }
        }

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

    private fun determineCircleMask(radius: Int = CIRCLE_RADIUS): Matrix<Boolean> {
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


    companion object {
        const val CIRCLE_RADIUS = 10

    }


}


fun main() {
    val circleLineTest = CircleLineTest()

    circleLineTest.extractLineSegments(32769)


}
