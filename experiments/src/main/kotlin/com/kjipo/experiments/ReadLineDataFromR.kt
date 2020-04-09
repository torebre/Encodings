package com.kjipo.experiments


import com.google.common.base.Functions
import com.kjipo.prototype.AngleLine
import com.kjipo.segmentation.Matrix
import com.kjipo.skeleton.transformToArrays
import com.kjipo.visualization.displayColourRasters
import javafx.scene.paint.Color
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors


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

    private fun groupLines(lines: List<AngleLine>) {
        val imageMatrix = createMatrix(lines, 0, 65, 0, 65)
        for (line in lines) {
            val neighbours = FindClosestNeighbours.extractNeighboursForLine(line, imageMatrix, lines)

            println("Neighbours: $neighbours")

        }
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


    @JvmStatic
    fun main(args: Array<String>) {
        val inputData = readData()
        val lineImage = inputData.iterator().next()

        groupLines(lineImage.value)
    }


}