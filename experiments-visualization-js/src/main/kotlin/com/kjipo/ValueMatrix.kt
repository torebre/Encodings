package com.kjipo

import com.kjipo.representation.Matrix
import getColour
import mu.KotlinLogging

internal class ValueMatrix(numberOfRows: Int, numberOfColumns: Int) {
    val valueMatrix: Matrix<Int>

    private val logger = KotlinLogging.logger {}


    init {
        valueMatrix = Matrix(numberOfRows, numberOfColumns) { _, _ -> 0 }
    }


    fun getColourForCoordinate(row: Int, column: Int): String? {
        return if (valueMatrix[row, column] == 0) {
            null
        } else {
            getColour(valueMatrix[row, column])
        }
    }

}