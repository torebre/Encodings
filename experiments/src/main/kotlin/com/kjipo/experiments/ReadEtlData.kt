package com.kjipo.experiments

import com.kjipo.segmentation.Matrix
import com.kjipo.visualization.displayMatrix
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object ReadEtlData {

    const val BASE_FILE_NAME = "/home/student/Downloads/ETL/ETL9B/ETL9B_"
    private const val STRUCT_LENGTH = 576


    @ExperimentalStdlibApi
    private fun readData(): List<Pair<Int, Matrix<Boolean>>> {
        return (1..5).map { Paths.get(BASE_FILE_NAME + it) }.map { parseFile(it) }.flatten().toList()
    }

    @ExperimentalStdlibApi
    private fun parseFile(file: Path): MutableList<Pair<Int, Matrix<Boolean>>> {
        val allInputData = Files.readAllBytes(file)
        val byteIterator = allInputData.iterator()

        for (j in 0 until STRUCT_LENGTH) {
            byteIterator.nextByte()
        }

        var recordCounter = 1
        val result = mutableListOf<Pair<Int, Matrix<Boolean>>>()

        while (byteIterator.hasNext()) {

            println("Reading record: ${++recordCounter}")

            val record = ByteArray(STRUCT_LENGTH)
            for (j in 0 until STRUCT_LENGTH) {
                record[j] = byteIterator.nextByte()
            }

            val serialSheetNumber = record[0].toInt().shl(8).plus(record[1])
            val jisKanjiCode = record[2].toInt().shl(8).plus(record[3])

            //                println("Serial sheet number: $serialSheetNumber. JIS kanji code: $jisKanjiCode")

            // The data is 63x64, but use a 64x64 matrix so that it is square
            val kanjiMatrix = Matrix(64, 64) { _, _ -> false }
            (0 until 63).map { row ->
                (0 until 8).map { offset -> decodeByte(record[8 + row * 8 + offset])
                        .forEachIndexed { index, b -> kanjiMatrix[row, offset * 8 + index] = b } }
            }

            result.add(Pair(jisKanjiCode, kanjiMatrix))

        }

        return result
    }


    @ExperimentalStdlibApi
    private fun decodeByte(input: Byte): BooleanArray {
        val result = BooleanArray(8)
        var transformedInput = input

        for (i in 0 until 8) {
            result[i] = transformedInput % 2 == 1

            if (result[i]) {
                transformedInput - 1
            }
            transformedInput = transformedInput.rotateRight(1)
        }

        return result.reversedArray()
    }


    @ExperimentalStdlibApi
    @JvmStatic
    fun main(args: Array<String>) {
//        readData()

        val parseFile = parseFile(Paths.get(BASE_FILE_NAME + 1))

        displayMatrix(parseFile[200].second, 5)


    }

}