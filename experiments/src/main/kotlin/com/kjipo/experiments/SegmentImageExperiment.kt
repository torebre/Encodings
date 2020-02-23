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


    private fun examineSegments(kanjiImage: Collection<Matrix<Boolean>>, length: Int) = kanjiImage.map { examineSegments(it, length) }.flatten()

    /**
     * Assuming the input image is quadratic
     */
    private fun examineSegments(kanjiImage: Matrix<Boolean>, length: Int) =
            kanjiImage.let {
                kanjiImage.numberOfRows.div(length) + if (kanjiImage.numberOfRows.rem(length) > 0) {
                    1
                } else {
                    0
                }
            }.let { numberOfSegmentsAlongDimension ->
                generateSequence(0) { if (it < numberOfSegmentsAlongDimension) it + 1 else null }.map { row ->
                    generateSequence(0) { if (it < numberOfSegmentsAlongDimension) it + 1 else null }.map { extractSegments(kanjiImage, row, it, length) }
                }.flatten().toList()
            }

    private fun extractSegments(kanjiImage: Matrix<Boolean>, startRow: Int, startColumn: Int, length: Int) =
            Matrix(length, length) { row, column ->
                when {
                    row >= kanjiImage.numberOfRows -> {
                        false
                    }
                    column >= kanjiImage.numberOfColumns -> {
                        false
                    }
                    else -> {
                        kanjiImage[row, column]
                    }
                }
            }

    private fun groupSegments(segments: Sequence<Matrix<Boolean>>) {
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

    private fun transformImage(encodedKanji: EncodedKanji) =
            makeThin(shrinkImage(transformArraysToMatrix(encodedKanji.image), 64, 64))


    @JvmStatic
    fun main(args: Array<String>) {
//        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8")).filter { it.unicode == 33541 }.toList()
        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8")).stream().limit(100).toList()

//        val transformedImage = transformImage(loadedKanji)

        groupSegments(examineSegments(loadedKanji.map { transformImage(it) }, 3).asSequence())

//        val lines = fitMultipleLinesUsingDevianceMeasure(transformedImage)

//        findClosestNeighbours(33541, lines, transformedImage, Paths.get("new_segment_extraction.csv"))
    }


}