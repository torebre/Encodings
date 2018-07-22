package com.kjipo.experiments

import com.kjipo.prototype.AngleLine
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.fitMultipleLinesUsingDevianceMeasure
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.visualization.loadKanjisFromDirectory
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.roundToInt


fun extractRelationData(lineExtractFunction: (image: Matrix<Boolean>) -> Collection<AngleLine>) {
    val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8"))
    val kanjiPrototypeMap = mutableMapOf<Int, Collection<AngleLine>>()

    Files.newBufferedWriter(Paths.get("position_data_2.csv")).use { outputWriter ->
        outputWriter.write("unicode, relative_length, angle_diff, start_pair_distance, relative_distance")
        outputWriter.newLine()

        loadedKanji.stream()
                .limit(5)
                .forEach {
                    val image = transformArraysToMatrix(it.image)
                    val linePrototypes = lineExtractFunction(image)

                    for (fromPrototype in linePrototypes) {
                        for (toPrototype in linePrototypes) {

                            val fromLength = if (fromPrototype.length.roundToInt() == 0) {
                                1.0
                            } else {
                                fromPrototype.length
                            }

                            val toLength = if (toPrototype.length.roundToInt() == 0) {
                                1.0
                            } else {
                                toPrototype.length
                            }

                            val relativeLength = fromLength.div(toLength)
                            val angleDiff = fromPrototype.angle.minus(toPrototype.angle)
                            val startPairDistance = Math.sqrt(Math.pow(fromPrototype.startPair.row.minus(toPrototype.startPair.row).toDouble(), 2.0) + Math.pow(fromPrototype.startPair.column.minus(toPrototype.startPair.column).toDouble(), 2.0))
                            val relativeStartDistance = startPairDistance.div(fromLength)

                            outputWriter.write(it.unicode.toString() + "," + relativeLength + "," + angleDiff + "," + startPairDistance + "," + relativeStartDistance)
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


fun extractLineFittedKanji(lineExtractFunction: (image: Matrix<Boolean>) -> Collection<AngleLine>) {
    val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8"))

    Files.newBufferedWriter(Paths.get("kanji_data_2.csv")).use { outputWriter ->
        outputWriter.write("unicode, line_number, angle, length, start_x, start_y")
        outputWriter.newLine()

        loadedKanji.stream()
                .limit(1)
                .forEach {
                    val image = transformArraysToMatrix(it.image)
                    val linePrototypes = lineExtractFunction(image)

                    var counter = 0
                    for (prototype in linePrototypes) {
                        val length = if (prototype.length.roundToInt() == 0) {
                            1.0
                        } else {
                            prototype.length
                        }

                        outputWriter.write(it.unicode.toString() + "," + counter
                                + "," + prototype.angle + "," + length
                                + "," + prototype.startPair.row + "," + prototype.startPair.column)
                        outputWriter.newLine()
                        ++counter
                    }

                }


    }
}


fun main(args: Array<String>) {
//    extractRelationData {
//        fitMultipleLinesUsingDevianceMeasure(shrinkImage(it, 64, 64))
//    }

    extractLineFittedKanji {
        fitMultipleLinesUsingDevianceMeasure(shrinkImage(it, 64, 64))
    }
}