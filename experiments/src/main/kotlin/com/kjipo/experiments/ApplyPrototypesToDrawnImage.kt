package com.kjipo.experiments

import com.kjipo.raster.match.MatchDistance
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.fitLinePrototypes
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.*
import com.kjipo.visualization.displayColourRasters
import com.kjipo.visualization.displayMatrix
import javafx.scene.paint.Color
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO


fun addPrototypesToDrawnImage(path: Path) {
    val colourRasters = mutableListOf<Array<Array<Color>>>()
    val texts = mutableListOf<String>()
    val readImage = ImageIO.read(path.toFile())


    val matrixImage = Matrix(readImage.height, readImage.width, { row, column -> false })
    for (row in 0 until readImage.height) {
        for (column in 0 until readImage.width) {
            if (readImage.getRGB(column, row) != -1) {

                matrixImage[row, column] = true
            }
        }
    }

//    val processedImage = shrinkImage(matrixImage, 32, 32)
    val processedImage = makeThin(makeSquare(matrixImage))

    val linePrototypes = fitLinePrototypes(processedImage)
    val distanceMatrix = transformArraysToMatrix(MatchDistance.computeDistanceMap(transformToBooleanArrays(processedImage)))

    var maxValue = Int.MIN_VALUE
    distanceMatrix.forEach {
        if (it > maxValue) {
            maxValue = it
        }
    }

    val dispImage = Matrix(processedImage.numberOfRows, processedImage.numberOfColumns, { row, column ->
        val distance = distanceMatrix[row, column]
        Color.hsb(distance.toDouble().div(maxValue).times(360), 0.5, 0.2)
    })

    displayMatrix(processedImage, 5)

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

    colourRasters.add(transformToArrays(dispImage))
    displayColourRasters(colourRasters, texts, 5)
}


fun main(args: Array<String>) {
    addPrototypesToDrawnImage(Paths.get("test2.png"))
}