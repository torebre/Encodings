package com.kjipo.experiments

import com.kjipo.prototype.AngleLine
import com.kjipo.raster.match.MatchDistance
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.fitLinePrototypes
import com.kjipo.segmentation.fitMultipleLinesUsingDevianceMeasure
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.makeThin
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToArrays
import com.kjipo.skeleton.transformToBooleanArrays
import com.kjipo.visualization.displayColourRasters
import com.kjipo.visualization.loadKanjisFromDirectory
import javafx.scene.paint.Color
import java.nio.file.Paths


object AddMultipleLinesUsingDevianceMeasureToCollectionOfKanji {

    private fun addLinePrototypes() {
        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8"))
        val colourRasters = mutableListOf<Array<Array<Color>>>()
        val texts = mutableListOf<String>()


        val kanjiPrototypeMap = mutableMapOf<Int, Collection<AngleLine>>()


        loadedKanji.stream()
                .limit(5)
//            .filter {
////                it.unicode == 33550 ||
////                it.unicode == 33897
//            }
                .forEach {
                    val image = transformArraysToMatrix(it.image)
                    val shrinkImage = shrinkImage(image, 64, 64)
                    val linePrototypes = fitMultipleLinesUsingDevianceMeasure(makeThin(shrinkImage))
                    val distanceMatrix = transformArraysToMatrix(MatchDistance.computeDistanceMap(transformToBooleanArrays(shrinkImage)))

                    var maxValue = Int.MIN_VALUE
                    distanceMatrix.forEach {
                        if (it > maxValue) {
                            maxValue = it
                        }
                    }

                    val dispImage = Matrix(shrinkImage.numberOfRows, shrinkImage.numberOfColumns, { row, column ->
                        //                        val distance = distanceMatrix[row, column]
//                        Color.hsb(distance.toDouble().div(maxValue).times(360), 0.5, 0.2)
                        if (image[row, column]) {
                            Color.WHITE
                        } else {
                            Color.BLACK
                        }
                    })

                    var counter = 0
                    linePrototypes.forEach {
                        it.segments.flatMap { it.pairs }.forEach {
                            if (it.row >= 0 && it.row < dispImage.numberOfRows && it.column >= 0 && it.column < dispImage.numberOfColumns) {
                                if (dispImage[it.row, it.column].brightness == 1.0) {
                                    dispImage[it.row, it.column] = Color.WHITE
                                } else {
                                    dispImage[it.row, it.column] = Color.hsb(counter.toDouble().div(linePrototypes.size).times(360), 1.0, 1.0)
                                }
                            }
                        }
                        ++counter
                    }

                    kanjiPrototypeMap.put(it.unicode, linePrototypes)

                    colourRasters.add(transformToArrays(dispImage))
                    texts.add(it.unicode.toString() + ": " + String(Character.toChars(it.unicode)))
                }

        displayColourRasters(colourRasters, texts, 2)


        kanjiPrototypeMap.forEach { unicode, prototypes ->
            val prototypesDescription = prototypes.map { it.startPair.let { it.row.toString() + "," + it.column } + "," + it.angle + "," + it.length + "," }.toString()
            println("Unicode: $unicode. Prototypes: $prototypesDescription")
            println("Unicode: $unicode. Number of prototypes: ${prototypes.size}")
        }


    }


    @JvmStatic
    fun main(args: Array<String>) {
        AddMultipleLinesUsingDevianceMeasureToCollectionOfKanji.addLinePrototypes()
    }


}