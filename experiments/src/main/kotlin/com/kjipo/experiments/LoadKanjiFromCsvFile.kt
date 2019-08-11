package com.kjipo.experiments

import com.kjipo.prototype.AngleLine
import com.kjipo.segmentation.Matrix
import com.kjipo.skeleton.transformToArrays
import com.kjipo.visualization.displayColourRasters
import javafx.scene.paint.Color
import java.lang.Math.abs
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

object LoadKanjiFromCsvFile {

    private fun loadKanjiFromCsvFile(kanjiFilePath: Path) {
        val unicodeLinesMap = readKanjiFile(kanjiFilePath)
        val (minX, maxX, minY, maxY) = findMinAndMax(unicodeLinesMap)
        val colourRasters = mutableListOf<Array<Array<Color>>>()
        val texts = mutableListOf<String>()

        var counter = 0
        unicodeLinesMap.forEach { unicode, lines ->
            val dispImage = Matrix(abs(maxX - minX), abs(maxY - minY), { row, column ->
                Color.BLACK
            })

            lines.forEach {
                it.segments.flatMap { it.pairs }.forEach {
                    if (it.row >= 0 && it.row < dispImage.numberOfRows && it.column >= 0 && it.column < dispImage.numberOfColumns) {
                        if (dispImage[it.row, it.column].brightness == 1.0) {
                            dispImage[it.row, it.column] = Color.WHITE
                        } else {
                            dispImage[it.row, it.column] = Color.hsb(counter.toDouble().div(lines.size).times(360), 1.0, 1.0)
                        }
                    }
                }
                ++counter
            }

            colourRasters.add(transformToArrays(dispImage))
            texts.add(unicode.toString() + ": " + String(Character.toChars(unicode)))
        }

        displayColourRasters(colourRasters, texts, 2)
    }

    fun findMinAndMax(unicodeLinesMap: Map<Int, List<AngleLine>>): List<Int> {
        var minX = 0
        var minY = 0
        var maxX = 0
        var maxY = 0

        unicodeLinesMap.values.flatMap { it.toList() }
                .forEach {
                    if (it.startPair.row < minX) {
                        minX = it.startPair.row
                    }
                    if (it.startPair.column < minY) {
                        minY = it.startPair.column
                    }
                    if (it.startPair.row > maxX) {
                        maxX = it.startPair.row
                    }
                    if (it.startPair.column > maxY) {
                        maxY = it.startPair.column
                    }

                    if (it.endPair.row < minX) {
                        minX = it.endPair.row
                    }
                    if (it.endPair.column < minY) {
                        minY = it.endPair.column
                    }
                    if (it.endPair.row > maxX) {
                        maxX = it.endPair.row
                    }
                    if (it.endPair.column > maxY) {
                        maxY = it.endPair.column
                    }
                }

        return listOf(minX, maxX, minY, maxY)
    }

    fun readKanjiFile(kanjiFilePath: Path): Map<Int, List<AngleLine>> {
        var counter = 0
        val unicodeLinesMap = Files.newBufferedReader(kanjiFilePath).use {
            return@use it.lines()
                    .skip(1)
                    .map {
                        val split = it.split(",")
                        val angleLine = Pair(split[0].toInt(), AngleLine(counter++, com.kjipo.raster.segment.Pair(split[4].toInt(), split[5].toInt()), split[3].toDouble(), split[2].toDouble()))

                        println("Unicode: ${angleLine.first}.  Line number: ${split[1]}. Start: ${angleLine.second.startPair}. End: ${angleLine.second.endPair}")
                        angleLine.second.segments[0].pairs.forEach { println("Line: $it") }

                        angleLine
                    }
                    .toList()
                    .groupBy({ it.first }, { it.second })
        }
        return unicodeLinesMap
    }


    @JvmStatic
    fun main(args: Array<String>) {
        loadKanjiFromCsvFile(Paths.get("kanji_data_2.csv"))

//        println("Line: ${computeLine(Pair(60, 20), Pair(27, 20))}")
    }
}