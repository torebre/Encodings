package com.kjipo.experiments

import com.kjipo.prototype.AngleLine
import com.kjipo.raster.match.MatchDistance
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.fitSingleLine3
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.makeThin
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToBooleanArrays
import com.kjipo.visualization.*
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import java.io.File
import javax.imageio.ImageIO

object FitMultipleLinesUsingFilledPixels {

        private fun fitLine() {
            val readImage = ImageIO.read(File("test2.png"))
            val image = Matrix(readImage.height, readImage.width, { row, column -> false })
            for (row in 0 until readImage.height) {
                for (column in 0 until readImage.width) {
                    if (readImage.getRGB(column, row) != -1) {
                        image[row, column] = true
                    }
                }
            }

            val shrinkImage = makeThin(shrinkImage(image, 64, 64))
            val fittedPrototypes = mutableListOf<AngleLine>()

            val tempImage = Matrix.copy(shrinkImage)
            var numberOfPixels = 0
            tempImage.forEach{ if(it) {++numberOfPixels }}
            var filledFixels = 0

            while(filledFixels != numberOfPixels) {
                var startPair = Pair(0, 0)
                tempImage.forEachIndexed { row, column, value ->
                    if (value) {
                        startPair = Pair(row, column)
                        return@forEachIndexed
                    }
                }


                println("Start pair: $startPair")

                val fittedLine = fitSingleLine3(tempImage, startPair)

                fittedLine.second.forEach {
                    if(tempImage[it.first, it.second]) {
                        ++filledFixels
                    }
                    tempImage[it.first, it.second] = false
                }

                fittedPrototypes.add(fittedLine.first)
            }

            val dispImage = Matrix(shrinkImage.numberOfRows, shrinkImage.numberOfColumns,  {row, column ->
                if(shrinkImage[row, column]) {
                    Color.WHITE
                }
                else {
                    Color.BLACK
                }
            })

            var counter = 0
            fittedPrototypes.forEach {
                it.segments.flatMap { it.pairs }.forEach {
                    if(it.row >= 0 && it.row < dispImage.numberOfRows && it.column >= 0 && it.column < dispImage.numberOfColumns) {
                        dispImage[it.row, it.column] = Color.hsb(counter.toDouble().div(fittedPrototypes.size).times(360), 1.0, 1.0)
                    }
                }
                ++counter
            }

            displayColourMatrix(dispImage, 5)
        }


        @JvmStatic
        fun main(args: Array<String>) {
            fitLine()


        }





}