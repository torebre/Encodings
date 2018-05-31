package com.kjipo.segmentation

import com.kjipo.prototype.AngleLine
import com.kjipo.prototype.FitPrototype
import com.kjipo.raster.attraction.PrototypeCollection
import com.kjipo.raster.match.MatchDistance
import com.kjipo.raster.segment.Pair
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToBooleanArrays
import kotlin.streams.toList


fun fitLinePrototypes(shrinkImage: Matrix<Boolean>): List<AngleLine> {
    val originalImage = Matrix.copy(shrinkImage)
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

    var filledPixels = 0

    for (i in 0 until 30) {
        var startPair = Pair(0, 0)
        shrinkImage.forEachIndexed({ row, column, value ->
            if (value) {
                startPair = Pair(row, column)
                return@forEachIndexed
            }
        })

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
        val prototype = (prototypes[0][0] as PrototypeCollection<AngleLine>).prototypes.iterator().next()
        filledPixels += prototype.segments.flatMap { it.pairs }.map {
            if (it.row < shrinkImage.numberOfRows && it.row >= 0 && it.column < shrinkImage.numberOfColumns && it.column >= 0 && originalImage[it.row, it.column]) {
                1
            } else {
                0
            }
        }.sum()

        allPrototypes.add(prototype)

        prototype.segments.forEach {
            it.pairs.forEach {
                if (it.row < shrinkImage.numberOfRows && it.row >= 0 && it.column < shrinkImage.numberOfColumns && it.column >= 0) {
                    shrinkImage[it.row, it.column] = false
                }
            }
        }


        if (filledPixels == totalFilledPixels) {
            break
        }

    }

    return allPrototypes
}

