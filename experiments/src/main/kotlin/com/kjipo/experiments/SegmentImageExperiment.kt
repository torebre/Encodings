package com.kjipo.experiments


import com.kjipo.representation.EncodedKanji
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.makeThin
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.visualization.loadKanjisFromDirectory
import java.nio.file.Paths
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.streams.toList

object SegmentImageExperiment {


    private fun examineSegments(kanjiImage: Collection<Matrix<Boolean>>, length: Int) = kanjiImage.map { examineSegments(it, length) }

    /**
     * Assuming the input image is quadratic
     */
    fun examineSegments(kanjiImage: Matrix<Boolean>, length: Int): Matrix<Matrix<Boolean>> {
        kanjiImage.let {
            kanjiImage.numberOfRows.div(length) + if (kanjiImage.numberOfRows.rem(length) > 0) 1 else 0
        }.let { numberOfSegmentsAlongDimension ->
            val emptyMatrix = Matrix(length, length) { _, _ -> false }
            val result = Matrix(numberOfSegmentsAlongDimension, numberOfSegmentsAlongDimension) { _, _ -> emptyMatrix }

            var currentRow = 0
            var currentColumn = 0

            generateSequence(0) { if (it < numberOfSegmentsAlongDimension - 1) it + 1 else null }.map { row ->
                generateSequence(0) { if (it < numberOfSegmentsAlongDimension - 1) it + 1 else null }.map { extractSegments(kanjiImage, row * length, it * length, length) }
            }
                    .flatten().forEach {
                        if (currentColumn >= numberOfSegmentsAlongDimension) {
                            currentColumn = 0
                            ++currentRow
                        }
                        result[currentRow, currentColumn] = it
                        ++currentColumn
                    }
            return result
        }

    }

    private fun extractSegments(kanjiImage: Matrix<Boolean>, startRow: Int, startColumn: Int, length: Int) =
            Matrix(length, length) { row, column ->
                when {
                    startRow + row >= kanjiImage.numberOfRows -> {
                        false
                    }
                    startColumn + column >= kanjiImage.numberOfColumns -> {
                        false
                    }
                    else -> {
                        kanjiImage[startRow + row, startColumn + column]
                    }
                }
            }

    fun groupSegments(segments: Sequence<Matrix<Boolean>>) {
        val segmentCounts = mutableMapOf<Long, Int>()
        segments.forEach {
            val key = generateKey(it)

            if (segmentCounts.containsKey(key)) {
                segmentCounts[key]?.let {
                    segmentCounts[key] = it + 1
                }

            } else {
                segmentCounts[key] = 1
            }
        }

        println("Grouping: $segmentCounts")
    }

    fun transformSegmentToGroups(segments: Matrix<Matrix<Boolean>>): Matrix<Matrix<Long>> {
        val emptyMatrix = Matrix(segments[0, 0].numberOfRows, segments[0, 0].numberOfColumns) { _, _ -> 0L }
        val result = Matrix(segments.numberOfRows, segments.numberOfColumns) { _, _ -> emptyMatrix }
        segments.forEachIndexed { row, column, matrix ->
            val key = generateKey(matrix)
            result[row, column] = Matrix(matrix.numberOfRows, matrix.numberOfColumns) { _, _ ->
                key
            }
        }
        return result
    }

    /**
     * Assuming the segments are quadratic
     */
    fun combineSegments(segments: Matrix<Matrix<Long>>): Matrix<Long> {
        val result = Matrix(segments.numberOfRows * segments[0, 0].numberOfRows, segments.numberOfRows * segments[0, 0].numberOfRows) { _, _ ->
            0L
        }

        segments.forEachIndexed { row, column, value ->
            value.forEachIndexed { subRow, subColumn, subValue ->
                result[row * value.numberOfRows + subRow, column * value.numberOfColumns + subColumn] = subValue
            }
        }

        return result
    }

    private fun generateKey(matrix: Matrix<Boolean>): Long {
        var key = 0L

        matrix.forEach { value ->
            key = key.shl(1).plus(if (value) 1 else 0)
        }
        return key
    }

    private fun Matrix<Boolean>.isEqual(otherMatrix: Matrix<Boolean>): Boolean {
        forEachIndexed { row, column, value ->
            if (otherMatrix[row, column] != value) {
                return false
            }
        }
        return true
    }

    fun transformImage(encodedKanji: EncodedKanji) = transformImage(encodedKanji.image)

    fun transformImage(image: Array<BooleanArray>) =
            makeThin(shrinkImage(transformArraysToMatrix(image), 64, 64))


    @JvmStatic
    fun main(args: Array<String>) {
        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8")).filter { it.unicode == 33541 }.toList()
//        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8")).stream().limit(100).toList()

//        val transformedImage = transformImage(loadedKanji)

//        groupSegments(examineSegments(loadedKanji.map { transformImage(it) }, 3).asSequence())

        val segments = examineSegments(transformImage(loadedKanji[0]), 3)
        combineSegments(transformSegmentToGroups(segments))

    }


}