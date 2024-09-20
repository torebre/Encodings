package com.kjipo.segmentation

import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.getNeighbourhood


class RegionExtractor {


    fun markEdgePixels(imageMatrix: Matrix<Boolean>): List<Pair<Int, Int>> {
        val edgePixels = mutableListOf<Pair<Int, Int>>()

        // Do not process the pixels at the edges of the image
        for (row in 1 until imageMatrix.numberOfRows - 1) {
            for (column in 1 until imageMatrix.numberOfColumns - 1) {
                if (imageMatrix[row, column]) {
                    val neighbourhood = getNeighbourhood(imageMatrix, row, column)
                    neighbourhood.forEachIndexed { innerRow, innerColumn, value ->
                        if (!value) {
                            val rowOffset = getOffset(innerRow)
                            val columnOffset = getOffset(innerColumn)
                            edgePixels.add(Pair(row + rowOffset, column + columnOffset))
                        }
                    }
                }
            }
        }

        return edgePixels
    }

}