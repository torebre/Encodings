package com.kjipo.experiments

import com.kjipo.prototype.AngleLine
import com.kjipo.raster.FlowDirection
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.fitMultipleLinesUsingDevianceMeasure
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.makeThin
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.visualization.loadKanjisFromDirectory
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.roundToInt


private object ExtractRelationDataFromKanji {


    fun extractRelationData(lineExtractFunction: (image: Matrix<Boolean>) -> Collection<AngleLine>, squareSide: Int) {
        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8"))
        val kanjiPrototypeMap = mutableMapOf<Int, Collection<AngleLine>>()

        Files.newBufferedWriter(Paths.get("position_data_5.csv")).use { outputWriter ->
            outputWriter.write("unicode, relative_length, angle_diff, start_pair_distance, relative_distance, start_pair_angle_diff, id_line1, id_line2")
            outputWriter.newLine()

            loadedKanji.stream()
//                .filter { t: EncodedKanji ->  t.unicode == 25022}
                    .limit(5000)
                    .forEach {

                        println("Examining kanji: ${it.unicode}")

                        val image = transformArraysToMatrix(it.image)
                        val linePrototypes = lineExtractFunction(image).toList()
                        var counter = 0

                        for (fromPrototype in linePrototypes) {
                            val lineFrom = counter++

                            for (toPrototype in extractRelevantLines(fromPrototype, linePrototypes, squareSide)) {
                                val lineTo = linePrototypes.indexOf(toPrototype)
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
                                val rowDiff = fromPrototype.startPair.row.minus(toPrototype.startPair.row)
                                val startPairAngleDiff = if (rowDiff == 0) {
                                    0.0
                                } else {
                                    Math.atan(fromPrototype.startPair.column.minus(toPrototype.startPair.column.toDouble() / rowDiff))
                                }

                                outputWriter.write(it.unicode.toString() + "," + relativeLength + "," + angleDiff + "," + startPairDistance
                                        + "," + relativeStartDistance + "," + startPairAngleDiff + "," + lineFrom + "," + lineTo)
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


    private fun findRelevantLines(lineExtractFunction: (image: Matrix<Boolean>) -> Collection<AngleLine>, squareSide: Int) {
        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8"), 1)

        loadedKanji.stream()
                .limit(1)
                .forEach {
                    val lines = lineExtractFunction(transformArraysToMatrix(it.image)).toList()
                    extractRelevantLines(lines.first(), lines, squareSide)
                }
    }


    private fun extractRelevantLines(inputLine: AngleLine, lines: List<AngleLine>, squareSide: Int): Collection<AngleLine> {
        val lineMatrix = Matrix<MutableSet<Int>?>(squareSide + 2, squareSide + 2)

        var lineCounter = 0
        var lineId = 0
        for (line in lines) {
            if (inputLine == line) {
                lineId = lineCounter
            }
//            println(line.segments[0].pairs)

            for (pair in line.segments[0].pairs) {
                if (lineMatrix[pair.row, pair.column] == null) {
                    lineMatrix[pair.row, pair.column] = mutableSetOf(lineCounter)
                } else {
                    lineMatrix[pair.row, pair.column]!!.add(lineCounter)
                }
            }
            ++lineCounter
        }

        val neighbouringLines = mutableSetOf<Int>()
//        val seenElements = mutableSetOf<Pair<Int, Int>>()

        var previousBorder = mutableSetOf<Pair<Int, Int>>()
        previousBorder.add(Pair(inputLine.startPair.row, inputLine.startPair.column))

        var border = FlowDirection.values().map {
            Pair(inputLine.startPair.row + it.rowShift, inputLine.startPair.column + it.columnShift)
        }
                .filter { it.first >= 0 && it.second >= 0 }
                .toMutableSet()


        var counter = 0
//            val images = mutableListOf<Matrix<Color>>()

        while (true) {
            counter++
//                if (counter == 100) {
//                    break
//                }

            var newBorder = border.map { borderCell ->
                FlowDirection.values().map {
                    val newBorderCandidate = Pair(borderCell.first + it.rowShift, borderCell.second + it.columnShift)

//                    FlowDirection.values().map {
//                        val row = newBorderCandidate.first + it.rowShift
//                        val column = newBorderCandidate.second + it.columnShift
//                    }

                    if (previousBorder.contains(newBorderCandidate)
                            || border.contains(newBorderCandidate)
                            || newBorderCandidate.first < 0
                            || newBorderCandidate.second < 0
                            || newBorderCandidate.first >= squareSide
                            || newBorderCandidate.second >= squareSide) {
                        null
                    } else {
                        newBorderCandidate
                    }
                }
            }
                    .flatten()
                    .filterNotNull()
                    .toMutableSet()

            if (newBorder.isEmpty()) {
                break
            }

            newBorder.forEach { pair ->
                lineMatrix[pair.first, pair.second]?.let {
                    neighbouringLines.addAll(it)
                }
            }

            if (neighbouringLines.size > 2) {
                break
            }

//                val matrix = Matrix(64, 64, { row, column ->
//                    val currentCell = Pair(row, column)
//                    if (newBorder.contains(currentCell)) {
//                        Color.RED
//                    } else if (border.contains(currentCell)) {
//                        Color.YELLOW
//                    } else if (lineMatrix[row, column] != null) {
//                        Color.BLUE
//                    } else {
//                        Color.WHITE
//                    }
//                })
//                images.add(matrix)

            previousBorder = border
            border = newBorder

//            println("New border: $newBorder")


        }

//            paintRaster(images.map {
//                transformToArrays(it)
//            }.toList())

//            break


        neighbouringLines.remove(lineId)
        return neighbouringLines.map { lines[it] }
    }


    fun extractLineFittedKanji(lineExtractFunction: (image: Matrix<Boolean>) -> Collection<AngleLine>) {
        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8"))

        Files.newBufferedWriter(Paths.get("kanji_data_full.csv")).use { outputWriter ->
            outputWriter.write("unicode, line_number, angle, length, start_x, start_y")
            outputWriter.newLine()

            loadedKanji.stream()
//                .limit(1)
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

                        println("Processed kanji: " + it.unicode)
                    }


        }
    }


    @JvmStatic
    fun main(args: Array<String>) {
//        extractRelationData({ fitMultipleLinesUsingDevianceMeasure(makeThin(shrinkImage(it, 64, 64))) }, 64)

//        extractLineFittedKanji {
//            fitMultipleLinesUsingDevianceMeasure(makeThin(shrinkImage(it, 64, 64)))
//        }

        findRelevantLines({
            fitMultipleLinesUsingDevianceMeasure(makeThin(shrinkImage(it, 64, 64)))
        }, 64)

    }


}