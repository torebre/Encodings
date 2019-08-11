package com.kjipo.experiments

import com.kjipo.raster.match.MatchDistance
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.fitSingleLine3
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToBooleanArrays
import javafx.scene.paint.Color
import java.io.File
import javax.imageio.ImageIO


private object FitLineUsingFilledPixels {


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

        val shrinkImage = shrinkImage(image, 64, 64)

        var startPair = Pair(0, 0)
        shrinkImage.forEachIndexed { row, column, value ->
            if (value) {
                startPair = Pair(row, column)
                return@forEachIndexed
            }
        }

        val fittedLine = fitSingleLine3(shrinkImage, startPair)

        println("Fitted line: $fittedLine")

        val distanceMatrix = transformArraysToMatrix(MatchDistance.computeDistanceMap(transformToBooleanArrays(shrinkImage)))
        var maxValue = Int.MIN_VALUE
        distanceMatrix.forEach {
            if (it > maxValue) {
                maxValue = it
            }
        }

        val dispImage = Matrix(shrinkImage.numberOfRows, shrinkImage.numberOfColumns, { row, column ->
            val distance = distanceMatrix[row, column]
            Color.hsb(distance.toDouble().div(maxValue).times(360), 0.5, 0.2)
        })

        showRaster(transformToBooleanArrays(shrinkImage), listOf(listOf(fittedLine.first)))


    }


    @JvmStatic
    fun main(args: Array<String>) {
        fitLine()


    }


}