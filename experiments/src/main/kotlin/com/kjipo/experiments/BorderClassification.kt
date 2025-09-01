package com.kjipo.experiments

import com.kjipo.representation.Matrix
import representation.backgroundRegion
import representation.borderRegion
import representation.extractBordersInBooleanMatrix
import representation.interiorPointRegion


class BorderClassification {

    fun extractBorderClassification(imageMatrix: Matrix<Boolean>): Matrix<Int> {
        val borders = extractBordersInBooleanMatrix(imageMatrix)

        val result = Matrix(
            imageMatrix.numberOfRows, imageMatrix.numberOfColumns,
            { row, column ->
                if (imageMatrix[row, column]) {
                    interiorPointRegion
                } else {
                    backgroundRegion
                }
            })

        for (border in borders) {
            for (pair in border.points) {
                result[pair.first, pair.second] = borderRegion
            }
        }

        return result
    }


}