package com.kjipo.experiments

import com.kjipo.prototype.AngleLine
import com.kjipo.prototype.FitPrototype
import com.kjipo.prototype.Prototype
import com.kjipo.raster.segment.Pair
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToArrays
import com.kjipo.visualization.loadEncodedKanji
import java.nio.file.Paths
import kotlin.streams.toList


fun addMultipleLinesPrototypesToSingleKanji() {
    val encodedKanji = loadEncodedKanji(Paths.get("kanji_output8/26681.dat"))
    val image = transformArraysToMatrix(encodedKanji.image)
    val shrinkImage = shrinkImage(image, 32, 32)
    val originalImage = Matrix.copy(shrinkImage)
    val allPrototypes = mutableListOf<Prototype>()

    for (i in 0 until 5) {
        val topPair = Pair.of(0, 0)
        val topId = 1
        val top = AngleLine(topId, topPair, 3.0, 0.0)

        val allLines = listOf(top)
        val fitPrototype = FitPrototype()

        val imageAsArrays = transformToArrays(shrinkImage)
        val prototypes = fitPrototype.addPrototypes(imageAsArrays, allLines, false).stream()
                .map { listOf(it) }
                .toList()

        // There is only one prototype in this case
        val prototype = prototypes[0][0]
        allPrototypes.add(prototype)

        prototype.segments.forEach {
            it.pairs.forEach {
                if(it.row < shrinkImage.numberOfRows && it.row >= 0 && it.column < shrinkImage.numberOfColumns && it.column >= 0) {
                    shrinkImage[it.row, it.column] = false
                }
            }
        }
    }

    showRaster(transformToArrays(originalImage), listOf(allPrototypes))

    Thread.sleep(java.lang.Long.MAX_VALUE)
}

fun main(args: Array<String>) {
    addMultipleLinesPrototypesToSingleKanji()
}