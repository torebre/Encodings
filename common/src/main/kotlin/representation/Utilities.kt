package representation

import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.EncodingUtilities
import com.kjipo.representation.raster.FlowDirection


const val backgroundRegion = 0
const val interiorPointRegion = 1
const val borderRegion = 2
const val startRegionCount = 10


fun identifyRegions(imageMatrix: Matrix<Boolean>, startRegionCounter: Int = startRegionCount): Matrix<Int> {
    val regionMatrix = Matrix<Int>(imageMatrix.numberOfRows, imageMatrix.numberOfColumns) { row, column ->
        if (imageMatrix[row, column]) {
            -1
        } else {
            backgroundRegion
        }
    }

    var fillValue = startRegionCounter
    var foundHit = true

    while (foundHit) {
        foundHit = false

        for (row in 0 until regionMatrix.numberOfRows) {
            for (column in 0 until regionMatrix.numberOfColumns) {
                if (regionMatrix[row, column] == -1) {
                    spreadAcrossRegion(row, column, fillValue, regionMatrix)
                    foundHit = true
                    ++fillValue
                }
            }
        }
    }

    return regionMatrix
}


private fun spreadAcrossRegion(
    startRow: Int, startColumn: Int, fillValue: Int,
    regionData: Matrix<Int>
) {
    val cellsToVisit = ArrayDeque<Pair<Int, Int>>()
    cellsToVisit.add(Pair(startRow, startColumn))

    while (cellsToVisit.isNotEmpty()) {
        val (row, column) = cellsToVisit.removeFirst()

        if (EncodingUtilities.validCoordinates(row, column, regionData.numberOfRows, regionData.numberOfColumns)
        ) {
            regionData[row, column] = fillValue
        }
        for (flowDirection in FlowDirection.entries) {
            val nextRow = row + flowDirection.rowShift
            val nextColumn = column + flowDirection.columnShift
            val nextPair = Pair(nextRow, nextColumn)

            if ((EncodingUtilities.validCoordinates(
                    nextRow,
                    nextColumn,
                    regionData.numberOfRows,
                    regionData.numberOfColumns
                )
                        && regionData[nextRow, nextColumn] == -1)
                && !cellsToVisit.contains(nextPair)
            ) {
                cellsToVisit.add(nextPair)
            }
        }
    }
}
