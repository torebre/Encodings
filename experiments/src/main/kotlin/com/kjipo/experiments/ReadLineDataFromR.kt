package com.kjipo.experiments


import com.google.gson.Gson
import com.kjipo.prototype.AngleLine
import com.kjipo.segmentation.Matrix
import com.kjipo.skeleton.transformToArrays
import com.kjipo.visualization.displayColourRasters
import javafx.scene.paint.Color
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt


object ReadLineDataFromR {

    private fun readData(): Map<Int, List<AngleLine>> {
        val inputData = Files.readAllLines(Paths.get("/home/student/workspace/kanjiR/training_data.csv"))

//        "unicode","line_number","angle","length","start_x","start_y"
        return inputData.stream().skip(1).map { line ->
            line.split(",").let {
                Pair(it[0].toInt(),
                        AngleLine(it[1].toInt(), com.kjipo.raster.segment.Pair(it[5].toInt(), it[4].toInt()), it[3].toDouble(), it[2].toDouble()))
            }
        }.collect(Collectors.toMap({
            it.first
        }, {
            mutableListOf(it.second)
        }, { old, newElement ->
            mutableListOf<AngleLine>().also {
                it.addAll(old)
                it.addAll(newElement)
            }
        }))

    }

    private fun displayData(codeLineMap: Map<Int, List<AngleLine>>) {
        val (minX, maxX, minY, maxY) = LoadKanjiFromCsvFile.findMinAndMax(codeLineMap)
        val colourRasters = mutableListOf<Array<Array<Color>>>()
        val texts = mutableListOf<String>()

        var counter = 0
        codeLineMap.forEach { unicode, lines ->
            val dispImage = Matrix(Math.abs(maxX - minX), Math.abs(maxY - minY)) { _, _ ->
                Color.BLACK
            }

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

    private fun groupLines(lines: List<AngleLine>, cutoff: Int = Int.MAX_VALUE) =
            createMatrix(lines, 0, 65, 0, 65).let { imageMatrix ->
                lines.map { line ->
                    val neighbours = FindClosestNeighbours.extractNeighboursForLine(line, imageMatrix, lines)

                    val sortedDistances = neighbours.values.sorted().distinct()
                    val cleanedCutoff = if (cutoff >= sortedDistances.size) {
                        Int.MAX_VALUE
                    } else {
                        sortedDistances[cutoff]
                    }

                    val relationData = neighbours.filter { it.value < cleanedCutoff }.map {
                        extractRelativePositionInformation(line, it.key)
                    }.toList()

                    Line(line.id, relationData)
                }.toList()
            }

    private fun extractRelativePositionInformation(inputLine: AngleLine, otherLine: AngleLine): RelativePositionInformation {
//        line2 <- all.lines.in.kanji[i, ]
//
//        # If a line has a length less than 1, round it up to 1
//        if (round(input.line$length) == 0) {
//            from.length <- 1
//        } else {
//            from.length <- input.line$length
//        }
//
//        if (round(line2$length) == 0) {
//            to.length <- 1
//        } else {
//            to.length <- line2$length
//        }

        val inputLineLength = if (inputLine.length < 1.0) 1.0 else inputLine.length
        val otherLineLength = if (otherLine.length < 1.0) 1.0 else otherLine.length

//
//        relative.length <- from.length / to.length
//        row.diff <- line2$start_y - input.line$start_y
//        column.diff <- line2$start_x - input.line$start_x
//        start.pair.distance <- sqrt(row.diff ^ 2 + column.diff ^ 2)

        val relativeLength = inputLineLength / otherLineLength
        val rowDiff = (otherLine.startPair.row - inputLine.startPair.row).toDouble()
        val columnDiff = (otherLine.startPair.column - inputLine.startPair.column).toDouble()

        val startPairDistance = sqrt(rowDiff * rowDiff + columnDiff * columnDiff)

//        line2.stop.x <- line2$start_x * cos(line2$angle)
//        line2.stop.y <- line2$start_y * sin(line2$angle)

        val otherLineStopX = otherLine.endPair.column
        val otherLineStopY = otherLine.endPair.row

//        switched.length <- sqrt((input.line$start_x - line2.stop.x)^2 + (input.line$start_y - line2.stop.y)^2)

        val switchedLength = sqrt((inputLine.startPair.column - otherLine.endPair.column.toDouble()).pow(2) + (inputLine.startPair.row - otherLine.endPair.row.toDouble()).pow(2))

//        # The smallest line is set to be one the is horizontal, and then the
//        # longer line is drawn relative to it
//        if(switched.length < start.pair.distance) {
//            start.pair.distance <- switched.length
//            start.pair.angle.diff <- pi/2 + atan2(row.diff, column.diff)
//        }
//        else {
//            start.pair.angle.diff <- atan2(row.diff, column.diff)
//        }

        val (distanceToUse, angleToUse) = if (switchedLength < startPairDistance) {
            Pair(switchedLength, Math.PI.div(2) + atan2(rowDiff, columnDiff))
        } else {
            Pair(startPairDistance, atan2(rowDiff, columnDiff))
        }

//        second.line.angle <- line2$angle - input.line$angle
//        start.pair.second.line.angle <- start.pair.angle.diff + input.line$angle
//
//        if(start.pair.second.line.angle < 0) {
//            start.pair.second.line.angle <- start.pair.second.line.angle + 2*pi
//        }

        val otherLineAngle = otherLine.angle - inputLine.angle
        val startPairSecondLineAngle = (angleToUse + inputLine.angle).let {
            if (it < 0) {
                2 * Math.PI
            } else {
                it
            }
        }

//        if(use.relative.distance) {
//            result[counter, 1] <- abs(row.diff) / input.line$length
//            result[counter, 2] <- abs(column.diff) / input.line$length
//        }
//        else {
//            result[counter, 1] <- abs(row.diff)
//            result[counter, 2] <- abs(column.diff)
//        }

//        result[counter, 3] <- start.pair.second.line.angle
//        result[counter, 4] <- line.number.input

        return RelativePositionInformation(abs(rowDiff).div(inputLineLength), abs(columnDiff).div(inputLineLength), startPairSecondLineAngle, inputLine.id, otherLine.id)
    }

    private fun createMatrix(lines: List<AngleLine>): Matrix<Boolean> {
        val (minX, maxX, minY, maxY) = LoadKanjiFromCsvFile.findMinAndMax(Collections.singletonMap(1, lines))
        return createMatrix(lines, minX, maxX, minY, maxY)
    }

    private fun createMatrix(lines: List<AngleLine>, minX: Int, maxX: Int, minY: Int, maxY: Int): Matrix<Boolean> {
        val dispImage = Matrix(Math.abs(maxX - minX), Math.abs(maxY - minY)) { _, _ ->
            false
        }

        lines.forEach { line ->
            line.segments.flatMap { it.pairs }.forEach {
                if (it.row >= 0 && it.row < dispImage.numberOfRows && it.column >= 0 && it.column < dispImage.numberOfColumns) {
                    dispImage[it.row, it.column] = true
                }
            }
        }


        return dispImage
    }


    data class Line(val id: Int, val relativePositions: Collection<RelativePositionInformation>)

    data class RelativePositionInformation(val rowDiff: Double, val colDiff: Double, val angle: Double, val inputLine: Int, val otherLine: Int)


    @JvmStatic
    fun main(args: Array<String>) {
        val inputData = readData()
//        val lineImage = inputData.iterator().next()

        val linesWithRelativePositionInformation = inputData.flatMap { entry ->
            groupLines(entry.value, 3)
        }.toList()

        val gson = Gson()
        Files.write(Paths.get("line_relative_position_information_rectangle_v2.json"), gson.toJson(linesWithRelativePositionInformation).toByteArray(StandardCharsets.UTF_8))
    }


}