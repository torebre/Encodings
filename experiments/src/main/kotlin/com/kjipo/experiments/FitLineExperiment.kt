package com.kjipo.experiments

import com.kjipo.representation.prototype.AngleLine
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.fitSingleLine2
import com.kjipo.segmentation.shrinkImage
import java.io.File
import javax.imageio.ImageIO


object FitLineExperiment {


    fun fitLine() {
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

        val startLine = AngleLine(1, com.kjipo.representation.segment.Pair(startPair.first, startPair.second), 0.0, 0.0)

        val fittedLine = fitSingleLine2(shrinkImage, startLine)

//        val distanceMatrix = transformArraysToMatrix(MatchDistance.computeDistanceMap(transformToBooleanArrays(shrinkImage)))


        println("Fitted line: $fittedLine")

    }


    @JvmStatic
    fun main(args: Array<String>) {
        fitLine()

    }


}