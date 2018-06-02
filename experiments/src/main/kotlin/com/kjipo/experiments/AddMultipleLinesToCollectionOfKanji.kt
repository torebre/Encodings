package com.kjipo.experiments

import com.kjipo.raster.match.MatchDistance
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.fitLinePrototypes
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToArrays
import com.kjipo.skeleton.transformToBooleanArrays
import com.kjipo.visualization.displayColourRasters
import com.kjipo.visualization.loadKanjisFromDirectory
import javafx.scene.paint.Color
import java.nio.file.Paths


fun addLinePrototypes() {
    val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8"))
    val colourRasters = mutableListOf<Array<Array<Color>>>()
    val texts = mutableListOf<String>()


    loadedKanji.stream()
            .limit(500)
//            .filter {
////                it.unicode == 33550 ||
////                it.unicode == 33897
//            }
            .forEach {
                val image = transformArraysToMatrix(it.image)
                val shrinkImage = shrinkImage(image, 32, 32)
                val linePrototypes = fitLinePrototypes(shrinkImage)
                val distanceMatrix = transformArraysToMatrix(MatchDistance.computeDistanceMap(transformToBooleanArrays(shrinkImage)))

                var maxValue = Int.MIN_VALUE
                distanceMatrix.forEach {
                    if (it > maxValue) {
                        maxValue = it
                    }
                }

                val dispImage = Matrix(shrinkImage.numberOfRows, shrinkImage.numberOfColumns, { row, column ->
                    val distance = distanceMatrix[row, column]
                    Color.hsb(distance.toDouble().div(maxValue).times(360), 0.5, 0.2)
                })

                var counter = 0
                linePrototypes.forEach {
                    it.segments.flatMap { it.pairs }.forEach {
                        if (it.row >= 0 && it.row < dispImage.numberOfRows && it.column >= 0 && it.column < dispImage.numberOfColumns) {
                            if(dispImage[it.row, it.column].brightness == 1.0) {
                                dispImage[it.row, it.column] = Color.WHITE
                            }
                            else {
                                dispImage[it.row, it.column] = Color.hsb(counter.toDouble().div(linePrototypes.size).times(360), 1.0, 1.0)
                            }
                        }
                    }
                    ++counter
                }

                colourRasters.add(transformToArrays(dispImage))
                texts.add(it.unicode.toString() +": " +String(Character.toChars(it.unicode)))
            }

    displayColourRasters(colourRasters, texts, 5)
}


fun main(args: Array<String>) {
    addLinePrototypes()
}