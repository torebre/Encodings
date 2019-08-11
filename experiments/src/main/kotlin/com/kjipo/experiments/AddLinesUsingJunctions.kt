package com.kjipo.experiments

import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.shrinkImage
import com.kjipo.setup.transformKanjiData
import com.kjipo.setupUtilities.EncodingUtilities.transformKanjiData
import com.kjipo.skeleton.extractJunctions
import com.kjipo.visualization.displayColourMatrix
import javafx.scene.paint.Color
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO


private object AddLinesUsingJunctions {


    fun addLinePrototypes() {
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
        val junctions = extractJunctions(shrinkImage)


        val dispImage = Matrix(shrinkImage.numberOfRows, shrinkImage.numberOfColumns, { row, column ->
            if (shrinkImage[row, column]) {
                Color.WHITE
            } else {
                Color.BLACK
            }
        })

        junctions.forEachIndexed { row, column, value ->
            if (value) {
                dispImage[row, column] = Color.RED
            }
        }

        displayColourMatrix(dispImage, 20)
    }


    @JvmStatic
    fun main(args: Array<String>) {
//        addLinePrototypes()


        val readImage = ImageIO.read(File("test2.png"))
        val image = Matrix(readImage.height, readImage.width, { row, column -> false })
        for (row in 0 until readImage.height) {
            for (column in 0 until readImage.width) {
                if (readImage.getRGB(column, row) != -1) {
                    image[row, column] = true
                }
            }
        }
        val transformKanjiData = transformKanjiData(image)
        Files.newBufferedWriter(Paths.get("input_image.csv")).use {
            it.write(transformKanjiData)
        }

    }


}