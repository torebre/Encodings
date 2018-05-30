package com.kjipo.experiments

import com.google.common.collect.ImmutableList
import com.kjipo.prototype.AngleLine
import com.kjipo.prototype.FitPrototype
import com.kjipo.prototype.Prototype
import com.kjipo.raster.match.MatchDistance
import com.kjipo.raster.segment.Pair
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToArrays
import com.kjipo.visualization.*
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import java.nio.file.Paths
import kotlin.streams.toList


fun addMultipleLinesPrototypesToSingleKanji() {
    val encodedKanji = loadEncodedKanji(Paths.get("kanji_output8/26681.dat"))
    val image = transformArraysToMatrix(encodedKanji.image)
    val shrinkImage = shrinkImage(image, 32, 32)
    val originalImage = Matrix.copy(shrinkImage)
    val allPrototypes = mutableListOf<Prototype>()

    var totalFilledPixels = 0
    shrinkImage.forEachIndexed({_, _, value ->
        if(value) {
            ++totalFilledPixels
        }
    })

    val prototypeImages = mutableListOf<Matrix<Color>>()

    val distanceMatrix = transformArraysToMatrix(MatchDistance.computeDistanceMap(transformToArrays(shrinkImage)))
    var maxValue = Int.MIN_VALUE
    distanceMatrix.forEach {
        if (it > maxValue) {
            maxValue = it
        }
    }

    for (i in 0 until 30) {
        var startPair = Pair(0, 0)
        shrinkImage.forEachIndexed({row, column, value ->
            if(value) {
                startPair = Pair(row, column)
                return@forEachIndexed
            }
        })

        val topPair = Pair.of(startPair.row, startPair.column)
        val topId = 1
        val top = AngleLine(topId, topPair, 0.0, 0.0)

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

        val dispImage = Matrix(shrinkImage.numberOfRows, shrinkImage.numberOfColumns, { row, column ->
            val distance = distanceMatrix[row, column]
            Color.hsb(distance.toDouble().div(maxValue).times(360), 0.5, 0.2)
        })

        var filledPixels = 0
        var counter = 0
        allPrototypes.forEach {
            it.segments.flatMap { it.pairs }.forEach {
                if(it.row >= 0 && it.row < dispImage.numberOfRows && it.column >= 0 && it.column < dispImage.numberOfColumns) {
                    if(originalImage[it.row, it.column]) {
                        ++filledPixels
                    }
                    dispImage[it.row, it.column] = Color.hsb(counter.toDouble().div(allPrototypes.size).times(360), 1.0, 1.0)
                }
            }
            ++counter
        }

        if(filledPixels == totalFilledPixels) {
            break
        }

        log.info("Remaining pixels: ${totalFilledPixels - filledPixels}. Total: $totalFilledPixels. Filled: $filledPixels")

        prototypeImages.add(Matrix.copy(dispImage))
    }




//    displayColourMatrix(dispImage, 20)



    var current = 0

    class Cell(val row: Int, val column: Int): CellType

    class ElementProcessor: RasterElementProcessor<Cell> {

        override fun processCell(cell: Cell?, squareSize: Int, node: ObservableList<Node>?, rectangle: Rectangle?) {
            cell?.let {
                rectangle?.fill = prototypeImages[current][it.row, it.column]
            }
        }
    }

    RasterVisualizer2.showRasterFlow<Cell>(
            object : RasterRun<Cell> {

                override fun getRawInput(): Array<BooleanArray> {
                    return transformToArrays(shrinkImage)
                }

                override fun hasNext(): Boolean {
                    return current < prototypeImages.size - 1
                }

                override fun getColumns(): Int {
                    return shrinkImage.numberOfColumns
                }

                override fun getRows(): Int {
                    return shrinkImage.numberOfRows
                }

                override fun getCell(row: Int, column: Int): Cell {
                    return Cell(row, column)
                }

                override fun next() {
                    ++current
                }
            },
            listOf(ElementProcessor()))

    showRaster(transformToArrays(originalImage), listOf(allPrototypes))
}

fun main(args: Array<String>) {
    addMultipleLinesPrototypesToSingleKanji()
}