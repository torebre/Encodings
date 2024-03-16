package com.kjipo.experiments

import com.kjipo.representation.Matrix
import java.nio.file.Files
import java.nio.file.Path
import java.util.*


fun loadKanjiMatrix(path: Path): Matrix<Boolean> {
    val bytesInFile = Files.readAllBytes(path)
    val bytes = Base64.getDecoder().decode(bytesInFile)
    val bitSet = BitSet.valueOf(bytes)
    val numberOfRows = bytes[0].toUByte().toInt()
    val numberOfColumns = bytes[1].toUByte().toInt()

    return Matrix(numberOfRows, numberOfColumns) { row, column ->
        bitSet[16 + row * numberOfRows + column]
    }
}
