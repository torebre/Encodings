package com.kjipo.segmentation

import com.kjipo.prototype.AngleLine
import com.kjipo.prototype.FitPrototype
import com.kjipo.raster.match.MatchDistance
import com.kjipo.raster.segment.Pair
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToBooleanArrays
import org.slf4j.LoggerFactory
import kotlin.streams.toList


val linePrototypeFittingLog = LoggerFactory.getLogger("LinePrototypeFitting")

fun fitLinePrototypes(originalImage: Matrix<Boolean>): List<AngleLine> {
    val shrinkImage = Matrix.copy(originalImage)
    val allPrototypes = mutableListOf<AngleLine>()

    var totalFilledPixels = 0
    shrinkImage.forEachIndexed({ _, _, value ->
        if (value) {
            ++totalFilledPixels
        }
    })

    val distanceMatrix = transformArraysToMatrix(MatchDistance.computeDistanceMap(transformToBooleanArrays(shrinkImage)))
    var maxValue = Int.MIN_VALUE
    distanceMatrix.forEach {
        if (it > maxValue) {
            maxValue = it
        }
    }


//    for (i in 0 until 50) {
    while (true) {
        var startPair = Pair(0, 0)
        shrinkImage.forEachIndexed({ row, column, value ->
            if (value) {
                startPair = Pair(row, column)
                return@forEachIndexed
            }
        })


        linePrototypeFittingLog.info("Start pair: $startPair")


        val topPair = Pair.of(startPair.row, startPair.column)
        val topId = 1
        val top = AngleLine(topId, topPair, 0.0, 0.0)

        val allLines = listOf(top)
        val fitPrototype = FitPrototype()

        val imageAsArrays = transformToBooleanArrays(shrinkImage)
        val prototypes = fitPrototype.addPrototypes(imageAsArrays, allLines, false).stream()
                .map { listOf(it) }
                .toList()


        // There is only one prototype in this case
//        val prototype = (prototypes[0][0] as PrototypeCollection<AngleLine>).prototypes.iterator().next()

        val prototype = prototypes[0][0] as AngleLine
        allPrototypes.add(prototype)

        var filledPixels = 0
        val tempImage = Matrix.copy(originalImage)

        linePrototypeFittingLog.info("Pairs: " + prototype.segments.flatMap { it.pairs }.toList())

        prototype.segments.flatMap { it.pairs }.let {
            var first = true

            for (pair in it) {
                if (first) {
                    first = false
                    shrinkImage[pair.row, pair.column] = false

                    continue
                }

                if (pair.row < shrinkImage.numberOfRows && pair.row >= 0 && pair.column < shrinkImage.numberOfColumns && pair.column >= 0) {
                    val centerRow = pair.row
                    val centerColumn = pair.column

                    fun fillFunction(row: Int, column: Int): Boolean {
                        val rowOffset = row - 1
                        val columnOffset = column - 1

                        val currentRow = centerRow - rowOffset
                        val currentColumn = centerColumn - columnOffset

                        return if (currentRow < 0 || currentRow >= shrinkImage.numberOfRows || currentColumn < 0 || currentColumn >= shrinkImage.numberOfColumns) {
                            false
                        } else {
                            shrinkImage[currentRow, currentColumn]
                        }
                    }

                    val tempMatrix = Matrix(3, 3, ::fillFunction)
                    val originalValue = shrinkImage[pair.row, pair.column]

                    if ((tempMatrix[1, 0] && tempMatrix[1, 1] && tempMatrix[1, 2])
                            || (tempMatrix[0, 1] && tempMatrix[1, 1] && tempMatrix[1, 2])
                            || (tempMatrix[0, 0] && tempMatrix[1, 1] && tempMatrix[2, 2])
                            || (tempMatrix[2, 2] && tempMatrix[1, 1] && tempMatrix[0, 2])) {
                        shrinkImage[pair.row, pair.column] = originalValue
                    } else {
                        shrinkImage[pair.row, pair.column] = false

                        /*
                        val regionsBeforeRemoval = FitPrototype.findNumberOfDisjointRegions(transformToBooleanArrays(tempMatrix))
                        shrinkImage[pair.row, pair.column] = false

                        val tempMatrix2 = Matrix(3, 3, ::fillFunction)
                        val regionsAfterRemoval = FitPrototype.findNumberOfDisjointRegions(transformToBooleanArrays(tempMatrix2))

                        val change = regionsBeforeRemoval - regionsAfterRemoval

                        linePrototypeFittingLog.info("regionsBeforeRemoval: $regionsBeforeRemoval, regionsAfterRemoval: $regionsAfterRemoval")

                        if (change != 0) {
                            shrinkImage[pair.row, pair.column] = originalValue
                        }
                        */
                    }


//                    shrinkImage[pair.row, pair.column] = false

                }
            }

        }

        allPrototypes.stream()
                .flatMap { it.segments.stream() }
                .flatMap { it.pairs.stream() }
                .forEach {
                    filledPixels += if (it.row < shrinkImage.numberOfRows && it.row >= 0 && it.column < shrinkImage.numberOfColumns && it.column >= 0 && tempImage[it.row, it.column]) {
                        tempImage[it.row, it.column] = false
                        1
                    } else {
                        0
                    }
                }


//        linePrototypeFittingLog.info("Total: $totalFilledPixels. Filled: $filledPixels")


        if (filledPixels == totalFilledPixels) {
            break
        }

    }

    return allPrototypes
}

