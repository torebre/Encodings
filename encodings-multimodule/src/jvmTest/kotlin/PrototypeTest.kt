import com.kjipo.matching.PrototypeStructure
import com.kjipo.readetl.EtlDataReader
import com.kjipo.readetl.EtlDataSet
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
import java.nio.file.Path
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
        val dataset = EtlDataReader.extractEtlImagesToKanjiData(
            Path.of("/home/student/Downloads/etlcbd_datasets"),
            EtlDataSet.ETL9G,
            5
        )

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


    private fun transformToBooleanMatrix(imageMatrix: Matrix<Int>): Matrix<Boolean> {
        return Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns, { _, _ -> false }).also { matrix ->
            imageMatrix.forEachIndexed { row, column, value -> matrix[row, column] = handlePixelValue(value) }
        }
    }


    private fun handlePixelValue(rgbValue: Int): Boolean {
//        if (rgbValue != -1) {
//            println("RBG value: $rgbValue")
//        }

        if (rgbValue != -1) {
//                            image[row, column] = true
//                        }
//                        else {
            // https://stackoverflow.com/questions/49676862/srgb-to-rgb-color-conversion
            val blue: Int = rgbValue and 255
            val green: Int = rgbValue shr 8 and 255
            val red: Int = rgbValue shr 16 and 255

            // https://www.baeldung.com/cs/convert-rgb-to-grayscale
            val grayscale = (0.3 * red) / 255 + (0.59 * green) / 255 + (0.11 * blue) / 255

            if (blue < 255 || green < 254 || red < 255) {
                println("Test30: $red, $green, $blue")
                println("Grayscale: $grayscale")
            }

            if (grayscale > 0.9) {
                return true
            }
        }
        return false

    }


    val colourMap = mapOf(Pair(1, IntArray(3).also { it[0] = 255; it[1] = 0; it[2] = 0 }),
        Pair(2, IntArray(3).also { it[0] = 0; it[1] = 255; it[2] = 0 })
    )


    fun runRegionExtraction() {
        val dataset = EtlDataReader.extractEtlImagesToKanjiData(
            Path.of("/home/student/Downloads/etlcbd_datasets"),
            EtlDataSet.ETL9G,
            1
        ).first()

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

        writeOutputMatrix(result, File("${dataset.filePath.fileName.toString().substringBefore('.')}_edges.png"), colourMap)
    }

    private fun writeOutputMatrix(result: Matrix<Int>, outputFile: File, colourMap: Map<Int, IntArray>) {
        val bufferedImage = BufferedImage(
            result.numberOfRows,
            result.numberOfColumns,
            TYPE_INT_RGB
        )

        result.forEachIndexed { row, column, value ->
            colourMap[value]?.let {
                bufferedImage.raster.setPixel(row, column, it)
            }
        }

        ImageIO.write(bufferedImage, "png", outputFile)
    }


    @JvmStatic
    fun main(args: Array<String>) {
//        createPrototypeStructure()
//        createSimplePrototypesFromEtlImages()

        runRegionExtraction()

    }


}