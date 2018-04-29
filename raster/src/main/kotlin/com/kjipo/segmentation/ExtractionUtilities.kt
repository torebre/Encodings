package com.kjipo.segmentation

import com.kjipo.prototype.AngleLine
import com.kjipo.raster.EncodingUtilities
import kotlin.math.floor
import kotlin.math.roundToInt


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

fun createRectangleFromEncompassingPoints(points: Collection<Pair<Int, Int>>): Matrix<Boolean> {
    val minRow = points.stream().mapToInt { it.first }.min().orElse(0)
    val maxRow = points.stream().mapToInt { it.first }.max().orElse(0)
    val minColumn = points.stream().mapToInt { it.second }.min().orElse(0)
    val maxColumn = points.stream().mapToInt { it.second }.max().orElse(0)

    val result = Matrix(maxRow - minRow + 1, maxColumn - minColumn + 1, { row, column -> false })

    points.forEach {
        result[it.first - minRow, it.second - minColumn] = true
    }

    return result
}


fun zoomRegion(image: Matrix<Boolean>, finalNumberOfRows: Int, finalNumberOfColumns: Int): Matrix<Boolean> {
    val deltaRow = image.numberOfRows.toDouble().div(finalNumberOfRows)
    val deltaColumn = image.numberOfColumns.toDouble().div(finalNumberOfColumns)

    return Matrix(finalNumberOfRows, finalNumberOfColumns, { row, column ->
        image[floor(deltaRow.times(row)).roundToInt(), floor(deltaColumn.times(column)).roundToInt()]
    })
}



fun shrinkImage(image: Matrix<Boolean>, finalNumberOfRows: Int, finalNumberOfColumns: Int): Matrix<Boolean> {
    val deltaRow = floor(image.numberOfRows.toDouble().div(finalNumberOfRows)).toInt()
    val deltaColumn = floor(image.numberOfColumns.toDouble().div(finalNumberOfColumns)).toInt()

    val kernelSize = deltaRow * deltaColumn
    val result = Matrix(finalNumberOfRows, finalNumberOfColumns, { row, column -> false })

    // TODO Do anything about the remainder?
    val rowRemainder = image.numberOfRows.rem(deltaRow * finalNumberOfRows)
    val columnRemainder = image.numberOfColumns.rem(deltaColumn * finalNumberOfColumns)

    var row2Counter = 0
    for (row in 0 until (image.numberOfRows - rowRemainder) step deltaRow) {
        var column2Counter = 0
        for (column in 0 until (image.numberOfColumns - columnRemainder) step deltaColumn) {

            var count = 0
            for (row2 in 0 until deltaRow) {
                for (column2 in 0 until deltaColumn) {
                    count += if (image[row + row2, column + column2]) {
                        1
                    } else {
                        0
                    }
                }
            }
//            if (count * 2 > kernelSize) {
//                result[row2Counter, column2Counter] = true
//            }

            // TODO Can be done more efficiently
            if(count > 0) {

                println("Count: $count")
                result[row2Counter, column2Counter] = true
            }
            ++column2Counter
        }
        ++row2Counter

    }

    return result
}


