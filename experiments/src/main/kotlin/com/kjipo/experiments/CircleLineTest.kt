package com.kjipo.experiments

import com.kjipo.readetl.EtlDataReader.extractEtlImagesForUnicodeToKanjiData
import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.makeSquare
import com.kjipo.representation.raster.makeThin
import com.kjipo.representation.raster.scaleMatrix
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class CircleLineTest {


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

            MatrixVisualization(matrix2) { value ->
                colourFunction(value, 5).let {
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
//        val matrix = Matrix.copy(matrixWithCenterLines)
//        val thinnedCopy = Matrix.copy(thinnedImage)

        val circleMask = determineCircleMask()

        // TODO Only here for testing
        applyCircleMask(thinnedImage.numberOfRows / 2, thinnedImage.numberOfColumns / 2, matrixWithCenterLines, circleMask)

//        for (row in 0 until matrix.numberOfRows) {
//            for (column in 0 until matrix.numberOfColumns) {
//                if (thinnedCopy[row, column]) {
//                    // TODO
//
//                    val circlePoints =
//                        determineCircle(row, column, thinnedImage.numberOfRows, thinnedImage.numberOfColumns, offsets)
//
//
//                }
//
//
//            }
//
//        }


    }

    private fun applyCircleMask(row: Int, column: Int, matrix: Matrix<Int>, circleMask: Matrix<Boolean>) {
        val rowCenter = circleMask.numberOfRows / 2
        val columnCenter = circleMask.numberOfColumns / 2

        val upperLeftRow = row - rowCenter
        val upperLeftColumn = column - columnCenter

        circleMask.forEachIndexed { rowMask, columnMask, value ->
            if(circleMask[rowMask, columnMask]) {
                val rowInMatrix = rowMask + upperLeftRow
                val columnInMatrix = columnMask + upperLeftColumn

                if (rowInMatrix >= 0
                    && rowInMatrix < matrix.numberOfRows
                    && columnInMatrix >= 0
                    && columnInMatrix < matrix.numberOfColumns
                ) {
                    // TODO Set some other value
                    matrix[rowInMatrix, columnInMatrix] = 1
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

    private fun determineCircleMask(): Matrix<Boolean> {
        val offsets = getOffsetsForCircle(CIRCLE_RADIUS)

        val maskMatrix = Matrix(2 * CIRCLE_RADIUS + 1, 2 * CIRCLE_RADIUS + 1) { _, _ ->
            true
        }

        val centerRow = maskMatrix.numberOfRows / 2
        val centerColumn = maskMatrix.numberOfColumns / 2

        offsets.forEach {
            maskMatrix[it.first + centerRow, it.second + centerColumn] = false
        }

        for (row in 0 until maskMatrix.numberOfRows) {
            for (column in 0 until maskMatrix.numberOfColumns) {
                if(!maskMatrix[row, column]) {
                    break
                }
                maskMatrix[row, column] = false
            }
        }

        for (row in maskMatrix.numberOfRows - 1 downTo 0) {
            for (column in maskMatrix.numberOfColumns - 1 downTo 0) {
                if(!maskMatrix[row, column]) {
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
        const val CIRCLE_RADIUS = 20

    }


}


fun main() {
    val circleLineTest = CircleLineTest()

    circleLineTest.extractLineSegments(32769)


}
