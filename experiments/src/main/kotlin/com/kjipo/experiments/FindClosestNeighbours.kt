package com.kjipo.experiments

import com.kjipo.experiments.RegionExtractionExperiments.writeSegmentLine
import com.kjipo.prototype.AngleLine
import com.kjipo.raster.match.MatchDistance
import com.kjipo.segmentation.fitMultipleLinesUsingDevianceMeasure
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.makeThin
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToBooleanArrays
import com.kjipo.visualization.loadKanjisFromDirectory
import java.nio.file.Paths

import com.kjipo.segmentation.Matrix
import java.nio.file.Files
import java.nio.file.Path


private object FindClosestNeighbours {


    private fun findClosestNeighbours(unicode: Int, lines : List<AngleLine>, image: Matrix<Boolean>, outputFile: Path) {
        Files.newBufferedWriter(outputFile).use { outputWriter ->
            var counter = 0
            var segmentCounter = 0
            for (line in lines) {
                val pairsInLine = line.segments.first().pairs
                val lineMatrix = Matrix(image.numberOfRows, image.numberOfColumns) { row, column ->
                    pairsInLine.contains(com.kjipo.raster.segment.Pair(row, column))
                }

                val distanceMatrix = transformArraysToMatrix(MatchDistance.computeDistanceMap(transformToBooleanArrays(lineMatrix)))

                val lineToMinimumDistanceMap = mutableMapOf<AngleLine, Int>()

                for (potentialNeighbour in lines) {
                    if (line == potentialNeighbour) {
                        continue
                    }

                    val linesInPotentialNeighbour = potentialNeighbour.segments.first()

                    val distances = linesInPotentialNeighbour.pairs.map {
                        distanceMatrix[it.row, it.column]
                    }

                    distances.min()?.let { minimumDistance ->
                        lineToMinimumDistanceMap[potentialNeighbour] = minimumDistance
                    }
                }

                val sortedDistances = lineToMinimumDistanceMap.values.sorted().distinct()

                for (i in 0..4) {
                    val cutoff = sortedDistances[i]

                    for (entry in lineToMinimumDistanceMap) {
                        if (entry.value <= cutoff) {
                            writeSegmentLine(unicode, i, entry.key.angle, entry.key.length, entry.key.startPair.row, entry.key.startPair.column, segmentCounter, outputWriter)
                        }
                    }
                    ++segmentCounter
                }

            }
        }

    }


    @JvmStatic
    fun main(args: Array<String>) {
        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8")).filter { it.unicode == 33541 }.toList()
        val transformedImage = makeThin(shrinkImage(transformArraysToMatrix(loadedKanji.first().image), 64, 64))
        val lines = fitMultipleLinesUsingDevianceMeasure(transformedImage)

        findClosestNeighbours(33541, lines, transformedImage, Paths.get("new_segment_extraction.csv"))
    }

}