package com.kjipo.experiments

import com.kjipo.experiments.RegionExtractionExperiments.writeSegmentLine
import com.kjipo.prototype.AngleLine
import com.kjipo.raster.match.MatchDistance
import com.kjipo.raster.segment.Pair
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.fitMultipleLinesUsingDevianceMeasure
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.makeThin
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToBooleanArrays
import com.kjipo.visualization.loadKanjisFromDirectory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


internal object FindClosestNeighbours {


    private fun findClosestNeighbours(unicode: Int, lines: List<AngleLine>, image: Matrix<Boolean>, outputFile: Path) {
        Files.newBufferedWriter(outputFile).use { outputWriter ->
            for (line in lines) {
                val lineToMinimumDistanceMap = extractNeighboursForLine(line, image, lines)
                val sortedDistances = lineToMinimumDistanceMap.values.sorted().distinct()

                var segmentCounter = 0
                for (i in 0..4) {
                    val cutoff = sortedDistances[i]

                    for (entry in lineToMinimumDistanceMap) {
                        if (entry.value <= cutoff) {
                            writeSegmentLine(unicode, i, entry.key.angle, entry.key.length, entry.key.startPair.row, entry.key.startPair.column, segmentCounter, outputWriter)
                        }
                    }
                    ++segmentCounter
                }

                println("Number of segments: $segmentCounter")
            }
        }
    }

    /**
     * Finds the shortest distance from {@param line} to each of the other lines. The result is
     * returned as a mapping between the lines and the shortest distances.
     */
    fun extractNeighboursForLine(line: AngleLine, image: Matrix<Boolean>, lines: List<AngleLine>): MutableMap<AngleLine, Int> {
        val pairsInLine = line.segments.first().pairs
        val lineMatrix = Matrix(image.numberOfRows, image.numberOfColumns) { row, column ->
            pairsInLine.contains(Pair(row, column))
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
        return lineToMinimumDistanceMap
    }


    @JvmStatic
    fun main(args: Array<String>) {
        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8")).filter { it.unicode == 33541 }.toList()
        val transformedImage = makeThin(shrinkImage(transformArraysToMatrix(loadedKanji.first().image), 64, 64))
        val lines = fitMultipleLinesUsingDevianceMeasure(transformedImage)

        findClosestNeighbours(33541, lines, transformedImage, Paths.get("new_segment_extraction.csv"))
    }

}