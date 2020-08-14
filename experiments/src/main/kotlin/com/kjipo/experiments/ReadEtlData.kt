package com.kjipo.experiments

import com.kjipo.segmentation.Matrix
import com.kjipo.visualization.displayMatrix
import java.awt.Transparency
import java.awt.image.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO

object ReadEtlData {

    private const val BASE_FILE_NAME = "/home/student/Downloads/ETL/ETL9B/ETL9B_"
    private const val STRUCT_LENGTH = 576

    data class KanjiData(val sheetNumber: Int, val kanjiCode: Int, val kanjiData: Matrix<Boolean>)


    @ExperimentalStdlibApi
    private fun readData(): List<KanjiData> {
        return (1..5).map { Paths.get(BASE_FILE_NAME + it) }.map { parseFile(it) }.flatten().toList()
    }

    @ExperimentalStdlibApi
    private fun parseFile(file: Path, limit: Int? = null): MutableList<KanjiData> {
        val allInputData = Files.readAllBytes(file)
        val byteIterator = allInputData.iterator()

        for (j in 0 until STRUCT_LENGTH) {
            byteIterator.nextByte()
        }

        var recordCounter = 1
        val result = mutableListOf<KanjiData>()

        byteLoop@ while (byteIterator.hasNext()) {

            println("Reading record: ${++recordCounter}")

            val record = ByteArray(STRUCT_LENGTH)
            for (j in 0 until STRUCT_LENGTH) {
                record[j] = byteIterator.nextByte()
            }

            val serialSheetNumber = record[1].toInt().shl(8).plus(record[0])
            val jisKanjiCode = record[3].toInt().shl(8).plus(record[2])

            println("Serial sheet number: $serialSheetNumber. JIS kanji code: $jisKanjiCode")

            // The data is 63x64, but use a 64x64 matrix so that it is square
            val kanjiMatrix = Matrix(64, 64) { _, _ -> false }

            val booleanValues = (0 until 504).map { it + 8 }
                    .map { decodeByte(record[it]) }
                    .flatMap { it.asIterable() }
                    .toList()

//            if(serialSheetNumber == 9219 && jisKanjiCode == 12068) {
//                println("Sum: ${booleanValues.map { if(it) { 1 } else { 0 }}.sum()}")
//            }


            for (row in 0 until 63) {
                for (column in 0 until 64) {
                    kanjiMatrix[row, column] = booleanValues[row * 64 + column]
                }
            }

//            (0 until 63).map { row ->
//                (0 until 8).map { offset ->
//                    decodeByte(record[8 + row * 8 + offset])
//                            .forEachIndexed { index, b -> kanjiMatrix[row, offset * 8 + index] = b }
//                }
//            }

            result.add(KanjiData(serialSheetNumber, jisKanjiCode, kanjiMatrix))

            if (limit == result.size) {
                break@byteLoop
            }
        }

        return result
    }


    @ExperimentalStdlibApi
    private fun decodeByte(input: Byte): BooleanArray {
        val result = BooleanArray(8)
        var transformedInput = input

        for (i in 0 until 8) {
            result[7 - i] = transformedInput.rem(2) != 0
            transformedInput = transformedInput.rotateRight(1)
        }

        return result
    }

    private fun writeImage(kanjiData: KanjiData, outputFile: Path) {
        val byteArray = ByteArray(3 * kanjiData.kanjiData.numberOfRows * kanjiData.kanjiData.numberOfColumns)

        for (row in 0 until kanjiData.kanjiData.numberOfRows) {
            for (column in 0 until kanjiData.kanjiData.numberOfColumns) {
                val pixelValue: Byte = if (kanjiData.kanjiData[row, column]) {
                    0
                } else {
                    -127
                }
                val offset = 3 * (row * kanjiData.kanjiData.numberOfRows + column)
                byteArray[offset] = pixelValue
                byteArray[offset + 1] = pixelValue
                byteArray[offset + 1] = pixelValue
            }
        }

        val buffer = DataBufferByte(byteArray, byteArray.size)
        val bands = IntArray(3)
        bands[0] = 0
        bands[1] = 1
        bands[2] = 2

        val raster = Raster.createInterleavedRaster(buffer, kanjiData.kanjiData.numberOfColumns, kanjiData.kanjiData.numberOfRows,
                kanjiData.kanjiData.numberOfColumns * 3, 3, bands, null)
        val colourModel = ComponentColorModel(ColorModel.getRGBdefault().colorSpace, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE)
        val image = BufferedImage(colourModel, raster, true, null)

        ImageIO.write(image, "png", outputFile.toFile())
    }

    @ExperimentalStdlibApi
    fun getEtlKanjiData(limit: Int? = null): List<KanjiData> {
        return parseFile(Paths.get(BASE_FILE_NAME + 2), limit)
    }


    @ExperimentalStdlibApi
    @JvmStatic
    fun main(args: Array<String>) {
//        readData()

        val parseFile = parseFile(Paths.get(BASE_FILE_NAME + 2))
        var counter = 0
        val outputDirectory = Paths.get("etl_output")
        if (!Files.exists(outputDirectory)) {
            Files.createDirectory(outputDirectory)
        }

        parseFile.forEach {
            val fileName = "${it.kanjiCode}_${it.sheetNumber}_${counter++}.png"
            writeImage(it, outputDirectory.resolve(fileName))
        }

//        parseFile.take(10).forEach {
//            println("Sheet number: ${it.sheetNumber}. Kanji code: ${it.kanjiCode}")
//        }

//        val kanjiExample = parseFile.first() {
//            it.sheetNumber == 9219 && it.kanjiCode == 12068
//        }
//        displayMatrix(kanjiExample.kanjiData, 5)

    }

}