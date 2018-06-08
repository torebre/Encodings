package com.kjipo.experiments

import com.kjipo.prototype.AngleLine
import com.kjipo.segmentation.fitLinePrototypes
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.visualization.loadKanjisFromDirectory
import javafx.scene.paint.Color
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.roundToInt


fun extractRelationData() {
    val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8"))
    val colourRasters = mutableListOf<Array<Array<Color>>>()
    val texts = mutableListOf<String>()

    val kanjiPrototypeMap = mutableMapOf<Int, Collection<AngleLine>>()

    Files.newBufferedWriter(Paths.get("position_data.csv")).use {
        val outputWriter = it

        loadedKanji.stream()
                .limit(5)
                .forEach {
                    val image = transformArraysToMatrix(it.image)
                    val shrinkImage = shrinkImage(image, 32, 32)
                    val linePrototypes = fitLinePrototypes(shrinkImage)




                    for (fromPrototype in linePrototypes) {
                        for (toPrototype in linePrototypes) {

                            val fromLength = if(fromPrototype.length.roundToInt() == 0) {
                                1.0
                            }
                            else {
                                fromPrototype.length
                            }

                            val toLength = if(toPrototype.length.roundToInt() == 0) {
                                1.0
                            }
                            else {
                                toPrototype.length
                            }

                            val relativeLength = fromLength.div(toLength)
                            val angleDiff = fromPrototype.angle.minus(toPrototype.angle)
                            val startPairDistance = Math.sqrt(Math.pow(fromPrototype.startPair.row.minus(toPrototype.startPair.row).toDouble(), 2.0) + Math.pow(fromPrototype.startPair.column.minus(toPrototype.startPair.column).toDouble(), 2.0))
                            val relativeStartDistance = startPairDistance.div(fromLength)

                            outputWriter.write(it.unicode.toString() + "," +relativeLength + "," + angleDiff + "," + startPairDistance + "," + relativeStartDistance)
                            outputWriter.newLine()
                        }
                    }
                }

    }


    kanjiPrototypeMap.forEach { unicode, prototypes ->
        val prototypesDescription = prototypes.map { it.startPair.let { it.row.toString() + "," + it.column } + "," + it.angle + "," + it.length + "," }.toString()
        println("Unicode: $unicode. Prototypes: $prototypesDescription")
        println("Unicode: $unicode. Number of prototypes: ${prototypes.size}")
    }


}


fun main(args: Array<String>) {
    extractRelationData()
}