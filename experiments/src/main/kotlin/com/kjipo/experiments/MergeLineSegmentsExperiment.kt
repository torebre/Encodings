package com.kjipo.experiments

import com.kjipo.prototype.AngleLine
import com.kjipo.raster.EncodingUtilities
import com.kjipo.raster.match.MatchDistance
import com.kjipo.representation.raster.FlowDirection
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.fitLinePrototypes
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToBooleanArrays
import com.kjipo.visualization.displayColourMatrix
import javafx.scene.paint.Color
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.absoluteValue


private object MergeLineSegmentsExperiment {


    fun mergeSegments() {
//        val encodedKanji = loadEncodedKanji(Paths.get("kanji_output8/26681.dat"))
//        val image = transformArraysToMatrix(encodedKanji.image)

        val readImage = ImageIO.read(File("test2.png"))
        val image = Matrix(readImage.height, readImage.width, { row, column -> false })
        for (row in 0 until readImage.height) {
            for (column in 0 until readImage.width) {
                if (readImage.getRGB(column, row) != -1) {
                    image[row, column] = true
                }
            }
        }

        val shrinkImage = shrinkImage(image, 64, 64)
        val linePrototypes = fitLinePrototypes(shrinkImage)
        val distanceMatrix = transformArraysToMatrix(MatchDistance.computeDistanceMap(transformToBooleanArrays(shrinkImage)))

//        val sortedPrototypes = linePrototypes.sortedBy { it.length }


        val lineMatrix = Matrix(shrinkImage.numberOfRows, shrinkImage.numberOfColumns, { row, column ->
            mutableListOf<Int>()
        })

        var counter2 = 0

        linePrototypes.forEach {
            it.segments.flatMap { it.pairs }.forEach {
                if (it.row >= 0 && it.row < shrinkImage.numberOfRows && it.column >= 0 && it.column < shrinkImage.numberOfColumns) {
                    lineMatrix[it.row, it.column].add(counter2)
                }
            }
            ++counter2
        }


        val updatedLinePrototypes = linePrototypes.map { AngleLine(it) }.toList()
        val linesToRemove = mutableSetOf<Int>()
        val expandedLines = mutableSetOf<Int>()


        updatedLinePrototypes.forEachIndexed { index, line ->

//            if (line.length < 2) {
                val startPairNeighbours = mutableSetOf<Int>()
//                val index = lineMatrix[line.startPair.row, line.startPair.column]

                FlowDirection.values().forEach {
                    if (EncodingUtilities.validCell(line.startPair.row, line.startPair.column, it, shrinkImage.numberOfRows, shrinkImage.numberOfColumns)) {
                        val neighbourRow = line.startPair.row + it.rowShift
                        val neighbourColumn = line.startPair.column + it.columnShift
                        val neighbourCell = lineMatrix[neighbourRow, neighbourColumn]
                        startPairNeighbours.addAll(neighbourCell)

                        neighbourCell.forEach {
                            if(it != index) {
                                if(// !expandedLines.contains(it) &&
                                        !linesToRemove.contains(it)) {

//                                    if(updatedLinePrototypes[it].startPair.row == neighbourRow && updatedLinePrototypes[it].startPair.column == neighbourColumn) {

                                        if(mergeLines(line, updatedLinePrototypes[it])) {
                                            println("Merging: ${line} and ${updatedLinePrototypes[it]}")

                                            updatedLinePrototypes[it].startPair = com.kjipo.raster.segment.Pair(line.startPair.row, line.startPair.column)
                                            linesToRemove.add(index)
                                            expandedLines.add(it)
                                        }
                                    }



//                                }
                            }

                        }
                    }
                }

                val neighbourDescription = startPairNeighbours.map {
                    it.toString() +"," +linePrototypes[it].startPair +"," +linePrototypes[it].length +"," +linePrototypes[it].angle +". "
                }

//                println("Start pair: ${line.startPair}. Length: ${line.length}. Angle: ${line.angle}. Neighbours: ${neighbourDescription}")

            }

//        }


        val updatedAndFilteredPrototypes = updatedLinePrototypes.filterIndexed {index, line ->
            !linesToRemove.contains(index)
        }.toList()


        var maxValue = Int.MIN_VALUE
        distanceMatrix.forEach {
            if (it > maxValue) {
                maxValue = it
            }
        }


//        val dispImage = Matrix(shrinkImage.numberOfRows, shrinkImage.numberOfColumns, { row, column ->
//            val distance = distanceMatrix[row, column]
//            Color.hsb(distance.toDouble().div(maxValue).times(360), 0.5, 0.2)
//        })
//
//        var counter = 0
//        linePrototypes.forEach {
//            it.segments.flatMap { it.pairs }.forEach {
//                if (it.row >= 0 && it.row < dispImage.numberOfRows && it.column >= 0 && it.column < dispImage.numberOfColumns) {
//                    if (dispImage[it.row, it.column].brightness == 1.0) {
//                        dispImage[it.row, it.column] = Color.WHITE
//                    } else {
//                        dispImage[it.row, it.column] = Color.hsb(counter.toDouble().div(linePrototypes.size).times(360), 1.0, 1.0)
//                    }
//                }
//            }
//            ++counter
//        }


        println("Number of lines: ${updatedAndFilteredPrototypes.size}")

        displayColourMatrix(createColourImage(updatedAndFilteredPrototypes, distanceMatrix, maxValue), 10)



//        displayColourMatrix(createColourImage(linePrototypes, distanceMatrix, maxValue), 10)
    }


    private fun mergeLines(line1: AngleLine, line2: AngleLine): Boolean {
        val line1EndPair = line1.endPair
        val line2StartPair = line2.startPair
        val distance = line1EndPair.row.minus(line2StartPair.row).absoluteValue + line1EndPair.column.minus(line2StartPair.column).absoluteValue

        return distance <= 2 && line1.angle.minus(line2.angle).absoluteValue < Math.PI / 8
    }



    private fun createColourImage(linePrototypes:List<AngleLine>, distanceMatrix:Matrix<Int>, maxDistance:Int): Matrix<Color> {
        val dispImage = Matrix(distanceMatrix.numberOfRows, distanceMatrix.numberOfColumns, { row, column ->
            val distance = distanceMatrix[row, column]
            Color.hsb(distance.toDouble().div(maxDistance).times(360), 0.5, 0.2)
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

        return dispImage
    }

    @JvmStatic
    fun main(args: Array<String>) {
        mergeSegments()

    }


}