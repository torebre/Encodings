package com.kjipo.readetl

import com.kjipo.representation.Matrix
import java.nio.file.Path
import java.util.*
import java.util.stream.Stream
import javax.imageio.ImageIO
import kotlin.io.path.*

/**
 * Contains methods for reading images from the ETL dataset extracted by
 * https://github.com/choo/etlcdb-image-extractor
 */
object EtlDataReader {


    fun extractEtlImagesToKanjiData(
        etlBaseDirectory: Path,
        dataset: EtlDataSet,
        maxNumberOfFilesToRead: Int = Int.MAX_VALUE
    ): List<KanjiFromEtlData> {
        if (!etlBaseDirectory.isDirectory()) {
            throw IllegalArgumentException("Input needs to be a directory")
        }

        var counter = 0

        return etlBaseDirectory.resolve(dataset.name).listDirectoryEntries().stream()
            .filter { it.isDirectory() }
            .flatMap { processEtlKanjiDirectory(it, dataset) }
            .takeWhile {
                counter++ < maxNumberOfFilesToRead
            }
            .toList()
    }

    private fun processEtlKanjiDirectory(etlKanjiDirectory: Path, dataset: EtlDataSet): Stream<KanjiFromEtlData> {
        val unicode = etlKanjiDirectory.name.let { HexFormat.fromHexDigits(it.substring(2)) }

        return etlKanjiDirectory.listDirectoryEntries().stream()
            .filter { it.extension == "png" }
            .map { KanjiFromEtlData(unicode, dataset, it, flipMatrix(readImage(it))) }
    }


    private fun readImage(imageFile: Path): Matrix<Int> {
        return ImageIO.read(imageFile.toFile()).let { bufferedImage ->
            Matrix(bufferedImage.height, bufferedImage.width) { _, _ -> 0 }.also { image ->
                for (row in 0 until bufferedImage.height) {
                    for (column in 0 until bufferedImage.width) {
                        val rgbValue = bufferedImage.getRGB(column, row)
                        image[row, column] = rgbValue
                    }
                }
            }
        }
    }

    private inline fun <reified T> flipMatrix(matrix: Matrix<T>): Matrix<T> {
        return Matrix(matrix.numberOfColumns, matrix.numberOfRows) { row, column ->
            matrix[column, row]
        }
    }


}

enum class EtlDataSet {
    ETL1,
    ETL2,
    ETL3,
    ETL4,
    ETL5,
    ETL6,
    ETL7,
    ETL8G,
    ETL9G
}


data class KanjiFromEtlData(
    val unicode: Int,
    val etlDataset: EtlDataSet,
    val filePath: Path,
    val kanjiData: Matrix<Int>
)


fun main() {
    val dataset = EtlDataReader.extractEtlImagesToKanjiData(
        Path.of("/home/student/Downloads/etlcbd_datasets"),
        EtlDataSet.ETL9G,
        5
    )

    dataset.forEach {
        println("${it.unicode}, ${it.etlDataset}, ${it.filePath}")
    }
}