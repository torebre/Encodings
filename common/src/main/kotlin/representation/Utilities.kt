package representation

import com.kjipo.representation.Matrix
import com.kjipo.representation.pointsmatching.Border
import com.kjipo.representation.raster.EncodingUtilities
import com.kjipo.representation.raster.FlowDirection


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


fun getConnectedPoints(row: Int, column: Int, borderMatrix: Matrix<Int>): MutableList<Pair<Int, Int>> {
    val firstPoint = Pair(row, column)
    val pointsToExamine = ArrayDeque(listOf(firstPoint))
    val borderMatrixCopy = Matrix.copy(borderMatrix)
    borderMatrixCopy[firstPoint.first, firstPoint.second] = backgroundRegion
    val borderPoints = mutableListOf<Pair<Int, Int>>()

    while (pointsToExamine.isNotEmpty()) {
        val point = pointsToExamine.removeFirst()
        borderPoints.add(point)

        FlowDirection.values().forEach { flowDirection ->
            if (EncodingUtilities.validCell(
                    point.first, point.second, flowDirection, borderMatrixCopy.numberOfRows,
                    borderMatrixCopy.numberOfColumns
                )
                && borderMatrixCopy[point.first + flowDirection.rowShift, point.second + flowDirection.columnShift] != backgroundRegion
            ) {
                Pair(point.first + flowDirection.rowShift, point.second + flowDirection.columnShift).let {
                    pointsToExamine.add(it)
                    borderMatrixCopy[it.first, it.second] = backgroundRegion
                }
            }
        }
    }

    return borderPoints
}

fun findBorderPoint(borderMatrix: Matrix<Int>, borderValue: Int): Pair<Int, Int>? {
    borderMatrix.forEachIndexed { row, column, _ ->
        if (borderMatrix[row, column] == borderValue) {
            return Pair(row, column)
        }
    }
    return null
}

fun extractBordersInMatrix(valueMatrix: Matrix<Int>): List<Border> {
    return extractBordersInMatrix(valueMatrix, valueExtractFunction = { row, column ->
        if (valueMatrix[row, column] == backgroundRegion) {
            backgroundRegion
        } else {
            interiorPointRegion
        }
    })
}

fun extractBordersInBooleanMatrix(valueMatrix: Matrix<Boolean>): List<Border> {
    return extractBordersInMatrix(valueMatrix, valueExtractFunction = { row, column ->
        if (valueMatrix[row, column]) {
            interiorPointRegion
        } else {
            backgroundRegion
        }
    })
}

inline fun <reified T> extractBordersInMatrix(valueMatrix: Matrix<T>, valueExtractFunction: (Int, Int) -> Int): List<Border> {
    val borderMatrix = Matrix(valueMatrix.numberOfRows, valueMatrix.numberOfColumns)
    { row, column ->
        valueExtractFunction(row, column)
    }

    val borders = mutableListOf<Border>()
    valueMatrix.forEachIndexed { row, column, value ->
        val neighbourhood = valueMatrix.getNeighbourhood<T>(row, column)
        var surroundedByEqualValues = true
        neighbourhood.forEach {
            if (it != null && it != value) {
                surroundedByEqualValues = false
                return@forEach
            }
        }

        if (surroundedByEqualValues) {
            borderMatrix[row, column] = backgroundRegion
        }
    }

    val borderMatrixCopy = Matrix.copy(borderMatrix)
    while (true) {
        val borderPoint = findBorderPoint(borderMatrixCopy, interiorPointRegion) ?: break
        val border = getConnectedPoints(borderPoint.first, borderPoint.second, borderMatrixCopy)
        borders.add(Border(border))

        border.forEach { borderMatrixCopy[it.first, it.second] = backgroundRegion }
    }

    return borders
}
