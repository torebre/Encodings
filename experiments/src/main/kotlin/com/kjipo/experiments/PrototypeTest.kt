package com.kjipo.experiments

import com.kjipo.matching.PrototypeStructure
import com.kjipo.representation.Matrix
import com.kjipo.representation.prototype2.EndpointPrototype
import com.kjipo.representation.prototype2.LinePrototype
import com.kjipo.representation.raster.bwmorphEndpoints
import com.kjipo.representation.raster.makeSquare
import com.kjipo.representation.raster.makeThin
import com.kjipo.segmentation.RegionExtractor
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import javax.imageio.ImageIO
import kotlin.io.path.name

object PrototypeTest {


    private fun createPrototypeStructure() {
        val prototypeStructure = PrototypeStructure()

        val linePrototype = LinePrototype("test1", EndpointPrototype("test1", 1, 2), EndpointPrototype("test1", 3, 4))
        val linePrototype2 = LinePrototype("test2", EndpointPrototype("test2", 2, 3), EndpointPrototype("test2", 5, 6))

        prototypeStructure.addPrototype(linePrototype)
        prototypeStructure.addPrototype(linePrototype2)
    }


    private fun createSimplePrototypesFromEtlImages() {
        val dataset = loadImagesFromEtl9G(5)

        dataset.forEach {
            println("${it.unicode}, ${it.etlDataset}, ${it.filePath}")
        }

        for (kanjiFromEtlData in dataset) {
            val squareImage = makeSquare(kanjiFromEtlData.kanjiData, { value ->
                handlePixelValue(value)
            }, 0).let {
                transformToBooleanMatrix(it)
            }

            val processedImage = makeThin(squareImage)
            val endpoints = bwmorphEndpoints(squareImage)

//            val bufferedImage = BufferedImage(processedImage.numberOfColumns,
//                processedImage.numberOfRows, TYPE_INT_RGB)

            val bufferedImage = BufferedImage(
                kanjiFromEtlData.kanjiData.numberOfColumns,
                kanjiFromEtlData.kanjiData.numberOfRows, TYPE_INT_RGB
            )

//            val endPointPixelValue = IntArray(3).also {
//                it[0] = 255
//                it[1] = 0
//                it[2] = 0
//            }

            val kanjiImagevalue = IntArray(3).also {
                it[0] = 255
                it[1] = 255
                it[2] = 255
            }

            processedImage.forEachIndexed { row, column, value ->
                if (value) {
                    bufferedImage.raster.setPixel(row, column, kanjiImagevalue)
                }
            }

            val outputFile = File("${kanjiFromEtlData.filePath.name.substringBefore('.')}_test_output.png")
            println("Writing file: $outputFile")
            ImageIO.write(bufferedImage, "png", outputFile)
        }

    }


    fun runRegionExtraction() {
        val dataset = loadImagesFromEtl9G(1).first()
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData))
        val regionExtractor = RegionExtractor()

        val edgePixels = regionExtractor.markEdgePixels(imageMatrix)
        val edgePixelsMatrix = Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns) { _, _ -> false }
        edgePixels.forEach {
            edgePixelsMatrix[it.first, it.second] = true
        }

        val result = Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns) { _, _ -> 0 }

        for (row in 0 until imageMatrix.numberOfRows) {
            for (column in 0 until imageMatrix.numberOfColumns) {
                if (edgePixelsMatrix[row, column]) {
                    result[row, column] = 1
                } else if (imageMatrix[row, column]) {
                    result[row, column] = 2
                }
            }
        }

        writeOutputMatrixToPngFile(
            result,
            File("${dataset.filePath.fileName.toString().substringBefore('.')}_edges.png"),
            colourMap
        )
    }



    @JvmStatic
    fun main(args: Array<String>) {
//        createPrototypeStructure()
//        createSimplePrototypesFromEtlImages()

        runRegionExtraction()

    }


}