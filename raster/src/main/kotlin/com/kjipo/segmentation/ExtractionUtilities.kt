package com.kjipo.segmentation

import com.kjipo.prototype.AngleLine
import com.kjipo.raster.EncodingUtilities


fun extractEmbeddedRegion(encodedKanji: Matrix<Boolean>, pairs: List<com.kjipo.raster.segment.Pair>, angle: Double, length: Double): List<Pair<Int, Int>> {
    val result = mutableListOf<Pair<Int, Int>>()

    for (pair in pairs) {
        val angleLine = AngleLine(-1, pair, length, angle)

        for (pair1 in angleLine.segments[0].pairs) {
            val row = pair1.row
            val column = pair1.column

            if (EncodingUtilities.validCoordinates(row, column, encodedKanji.numberOfRows, encodedKanji.numberOfColumns)) {
                result.add(Pair(row, column))
            }
        }
    }

    return result
}