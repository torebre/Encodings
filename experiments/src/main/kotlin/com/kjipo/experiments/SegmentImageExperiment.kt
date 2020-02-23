package com.kjipo.experiments


import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.makeThin
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.visualization.loadKanjisFromDirectory
import java.nio.file.Paths

object SegmentImageExperiment {


    /**
     * Assuming the input image is quadratic
     */
    private fun examineSegments(kanjiImage: Matrix<Boolean>, length: Int) {
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


    @JvmStatic
    fun main(args: Array<String>) {
        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8")).filter { it.unicode == 33541 }.toList()
        val transformedImage = makeThin(shrinkImage(transformArraysToMatrix(loadedKanji.first().image), 64, 64))

        examineSegments(transformedImage, 3)

//        val lines = fitMultipleLinesUsingDevianceMeasure(transformedImage)

//        findClosestNeighbours(33541, lines, transformedImage, Paths.get("new_segment_extraction.csv"))
    }


}