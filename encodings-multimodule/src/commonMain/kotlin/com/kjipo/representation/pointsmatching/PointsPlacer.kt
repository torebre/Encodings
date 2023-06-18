package com.kjipo.representation.pointsmatching

import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.*
import kotlin.math.pow
import kotlin.math.sqrt

class PointsPlacer(private val imageMatrix: Matrix<Boolean>) {
    private val points = mutableListOf<Pair<Int, Int>>()

    val regionMatrix: Matrix<Int> by lazy {
        identifyRegions()
    }

    val linePointMatrix: Matrix<Double> by lazy {
        identifyLinePoints()
    }

    val centerOfMassMatrix: Matrix<Int> by lazy {
        centerOfMassPointsForRegions()
    }

    val flowDirectionDirectionMap = mapOf(
        Pair(FlowDirection.EAST, Direction.LEFT_RIGHT),
        Pair(FlowDirection.NORTH_EAST, Direction.DIAGONAL_UP),
        Pair(FlowDirection.NORTH, Direction.UP_DOWN),
        Pair(FlowDirection.NORTH_WEST, Direction.DIAGONAL_DOWN),
        Pair(FlowDirection.WEST, Direction.LEFT_RIGHT),
        Pair(FlowDirection.SOUTH_WEST, Direction.DIAGONAL_UP),
        Pair(FlowDirection.SOUTH, Direction.UP_DOWN),
        Pair(FlowDirection.SOUTH_EAST, Direction.DIAGONAL_DOWN)
    )


    fun runPlacement() {
        val firstPoint = addFirstPoint()
        points.add(firstPoint)

        addPoint(firstPoint)?.let {
            points.add(it)
        }

        removeInteriorPoints(regionMatrix)

    }


    fun identifyLinePoints(): Matrix<Double> {
        val linePointMatrix = Matrix(regionMatrix.numberOfRows, regionMatrix.numberOfColumns) { _, _ -> 0.0 }
        regionMatrix.forEachIndexed { row, column, _ ->
            val directionRatio = checkLinePoint(row, column, regionMatrix)
            linePointMatrix[row, column] = directionRatio.second
        }
        return linePointMatrix
    }

    fun centerOfMassPointsForRegions(): Matrix<Int> {
        val centerOfMassPointsMatrix = Matrix(
            regionMatrix.numberOfRows,
            regionMatrix.numberOfColumns
        ) { _, _ -> 0 }
        var regionCounter = startRegionCount

        while (true) {
            val massCenter = findMassCenter(regionMatrix, regionCounter) ?: break

            centerOfMassPointsMatrix[massCenter.first, massCenter.second] = 1
            ++regionCounter
        }

        return centerOfMassPointsMatrix
    }

    private fun removeInteriorPoints(valueMatrix: Matrix<Int>) {
        val pointsInInterior = mutableListOf<Pair<Int, Int>>()

        valueMatrix.forEachIndexed { row, column, value ->
            val neighbourhood = valueMatrix.getNeighbourhood<Int?>(row, column)

            var surroundedByEqualValues = true
            neighbourhood.forEach {
                if (it != null && it != value) {
                    surroundedByEqualValues = false
                    return@forEach
                }
            }

            if (surroundedByEqualValues) {
                pointsInInterior.add(Pair(row, column))
            }
        }

        pointsInInterior.forEach {
            valueMatrix[it.first, it.second] = interiorPointRegion
        }
    }


    private fun addFirstPoint(): Pair<Int, Int> {
        // TODO For now just returning the first point with a value
        imageMatrix.forEachIndexed { row, column, value ->
            if (value) {
                return Pair(row, column)
            }
        }

        throw IllegalStateException("Image is blank")
    }


    private fun addPoint(existingPoint: Pair<Int, Int>): Pair<Int, Int>? {
        // TODO

        val neighbourhood = imageMatrix.getNeighbourhood<Boolean>(existingPoint.first, existingPoint.second)

        var temp: Pair<Int, Int>? = null
        neighbourhood.forEachIndexed { row, column, value ->
            if (value == true) {
                temp = Pair(row + existingPoint.first, column + existingPoint.second)
                return@forEachIndexed
            }
        }

        if (temp == null) {
            // Nowhere to put point
            return null
        }

        return temp?.let {
            movePoint(it)
        }

    }


    private fun movePoint(newPoint: Pair<Int, Int>): Pair<Int, Int> {
        var currentPoint = newPoint
        var counter = 0

        while (counter < 100) {
            val updatedPoint = doOnePointStep(currentPoint)

            if (currentPoint == updatedPoint) {
                return currentPoint
            }

            currentPoint = updatedPoint
            ++counter
        }

        return currentPoint
    }


    private fun doOnePointStep(point: Pair<Int, Int>): Pair<Int, Int> {
        val neighbourhood = imageMatrix.getNeighbourhood<Boolean>(point.first, point.second)
        val distances = Matrix<Double?>(3, 3) { _, _ -> null }

        for (row in 0 until 3) {
            for (column in 0 until 3) {
                neighbourhood[row, column]?.let { value ->
                    if (!value) {
                        return@let
                    }

                    val distance = points.sumOf { point ->
                        sqrt(
                            (point.first - point.first).toDouble().pow(2)
                                    + (point.second - point.second).toDouble().pow(2)
                        )
                    }
                    distances[row, column] = distance
                }
            }
        }

        var direction: FlowDirection? = null
        neighbourhood.forEachIndexed { row, column, value ->
            if (value == true) {
                getFlowDirectionForOffset(row - 1, column - 1)?.let {
                    direction = it
                    return@forEachIndexed
                }
            }
        }

        if (direction == null) {
            return point
        }

        for (row in 0 until 3) {
            for (column in 0 until 3) {
                distances[row, column]?.let { distanceToPoint ->
                    direction?.let { currentDirection ->
                        val distance = distances[currentDirection.rowShift + 1, currentDirection.columnShift + 1]

                        if (distance != null && distance > distanceToPoint) {
                            getFlowDirectionForOffset(row - 1, column - 1)?.let {
                                direction = it
                            }
                        }
                    }
                }
            }
        }

        return direction?.let {
            Pair(point.first + it.rowShift, point.second + it.columnShift)
        } ?: point

    }


    private fun identifyRegions(): Matrix<Int> {
        val regionMatrix = Matrix<Int>(imageMatrix.numberOfRows, imageMatrix.numberOfColumns) { row, column ->
            if (imageMatrix[row, column]) {
                -1
            } else {
                0
            }
        }

        var fillValue = startRegionCount
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
            for (flowDirection in FlowDirection.values()) {
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


    fun checkLinePoint(row: Int, column: Int, imageMatrix: Matrix<Int>): Pair<FlowDirection, Double> {
        val directionRatioMap = mutableMapOf<FlowDirection, Double>()

        for (direction in FlowDirection.values()) {
            var stepsMainDirection = 0
            var mainDirectionX = row
            var mainDirectionY = column

            while (true) {
                mainDirectionX += direction.rowShift
                mainDirectionY += direction.columnShift

                if (!imageMatrix.isValid(mainDirectionX, mainDirectionY)
                    || imageMatrix[mainDirectionX, mainDirectionY] == backgroundRegion
                ) {
                    break
                }
                ++stepsMainDirection
            }

            var stepsSecondaryDirection = 0
            val secondaryDirection = direction.shiftTwoStepsForward()
            var secondaryDirectionX = row
            var secondaryDirectionY = column

            while (true) {
                secondaryDirectionX += secondaryDirection.rowShift
                secondaryDirectionY += secondaryDirection.columnShift

                if (!imageMatrix.isValid(secondaryDirectionX, secondaryDirectionY)
                    || imageMatrix[secondaryDirectionX, secondaryDirectionY] == backgroundRegion
                ) {
                    break
                }
                ++stepsSecondaryDirection
            }

            directionRatioMap[direction] = stepsMainDirection.toDouble() / stepsSecondaryDirection
        }

        var maxDirection = FlowDirection.EAST
        var maxRatio = directionRatioMap[FlowDirection.EAST]!!
        directionRatioMap.forEach { value ->
            if (value.value > maxRatio) {
                maxDirection = value.key
                maxRatio = value.value
            }
        }

        return Pair(maxDirection, maxRatio)
    }


    fun extractBorders(valueMatrix: Matrix<Int> = regionMatrix): List<Border> {
        val borderMatrix = Matrix(valueMatrix.numberOfRows, valueMatrix.numberOfColumns)
        { row, column ->
            if (valueMatrix[row, column] == backgroundRegion) {
                backgroundRegion
            } else {
                interiorPointRegion
            }
        }

        val borders = mutableListOf<Border>()

        valueMatrix.forEachIndexed { row, column, value ->
            val neighbourhood = valueMatrix.getNeighbourhood<Int?>(row, column)
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

    fun extractBorderStructure(valueMatrix: Matrix<Int> = regionMatrix): ImageStructure {
        val imageStructure = ImageStructure(extractBorders(valueMatrix))

        return imageStructure
    }


    fun encodeBorderIntoDirectionList(singleBorderMatrix: Matrix<Int>): List<PointDirection> {
        singleBorderMatrix.forEachIndexed { row, column, value ->
            if (singleBorderMatrix[row, column] != backgroundRegion) {
                return encodeBorderIntoDirectionList(row, column, singleBorderMatrix) {
                    it != backgroundRegion
                }
            }
        }
        return emptyList()
    }


    inline fun <reified T> encodeBorderIntoDirectionList(
        row: Int,
        column: Int,
        valueMatrix: Matrix<T>,
        noinline valueFunction: (T) -> Boolean
    ): List<PointDirection> {
        val pointsToExamine = ArrayDeque<Pair<Int, Int>>()
            .also { it.add(Pair(row, column)) }

        val matrixCopy = Matrix(valueMatrix.numberOfRows, valueMatrix.numberOfColumns) { row2, column2 ->
            valueFunction(valueMatrix[row2, column2])
        }

        val borderPoints = mutableListOf<PointDirection>()

        val directionList = arrayOf(
            FlowDirection.EAST,
            FlowDirection.SOUTH_EAST,
            FlowDirection.NORTH_EAST,
            FlowDirection.SOUTH,
            FlowDirection.SOUTH_WEST,
            FlowDirection.WEST,
            FlowDirection.NORTH_WEST,
            FlowDirection.NORTH,
        )
        while (pointsToExamine.isNotEmpty()) {
            val point = pointsToExamine.removeFirst()
            val direction = classifyDirection(
                point.first, point.second,
                valueMatrix,
                valueFunction
            )

            borderPoints.add(PointDirection(point.first, point.second, direction))
            matrixCopy[point.first, point.second] = false

            getNeighbourhood(
                matrixCopy,
                point.first,
                point.second,
                directionList
            ).forEach { directionValidInformation ->
                if (directionValidInformation.second
                    && matrixCopy[point.first + directionValidInformation.first.rowShift, point.second + directionValidInformation.first.columnShift]
                ) {
                    pointsToExamine.add(
                        Pair(
                            point.first + directionValidInformation.first.rowShift,
                            point.second + directionValidInformation.first.columnShift
                        )
                    )
                    return@forEach
                }
            }
        }

        return borderPoints
    }


    /**
     * Given a point examines the direction of the line going through the point
     * if possible.
     */
    inline fun <reified T> classifyDirection(
        row: Int,
        column: Int,
        borderMatrix: Matrix<T>,
        noinline valueFunction: (T) -> Boolean
    ): Direction {
        if (!valueFunction(borderMatrix[row, column])) {
            // Not determining any direction if the center point is missing
            return Direction.NONE
        }

        val neighbourHood = getNeighbourhoodType(borderMatrix, row, column, valueFunction)
        var occupiedCells = 0

        neighbourHood.forEach { occupiedCells += if (it) 1 else 0 }

        if (occupiedCells != 2) {
            // Two of the cells around the center should be occupied to be able to determine a direction
            return Direction.NONE
        }

        for (i in 0 until FlowDirection.values().count() / 2) {
            val direction = FlowDirection.values()[i]
            val oppositeDirection = direction.oppositeDirection()

            if (neighbourHood[direction.rowShift + 1, direction.columnShift + 1] &&
                neighbourHood[oppositeDirection.rowShift + 1, oppositeDirection.columnShift + 1]
            ) {
                // The direction should be in the map if this point is reached
                return flowDirectionDirectionMap[direction]!!
            }
        }

        // Should not reach this point
        return Direction.NONE
    }

    private fun findBorderPoint(borderMatrix: Matrix<Int>, borderValue: Int): Pair<Int, Int>? {
        borderMatrix.forEachIndexed { row, column, _ ->
            if (borderMatrix[row, column] == borderValue) {
                return Pair(row, column)
            }
        }
        return null
    }

    private fun getConnectedPoints(row: Int, column: Int, borderMatrix: Matrix<Int>): MutableList<Pair<Int, Int>> {
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

    private fun findMassCenter(imageMatrix: Matrix<Int>, regionId: Int): Pair<Int, Int>? {
        var rowSum = 0
        var columnSum = 0
        var numberOfCellsIncluded = 0

        imageMatrix.forEachIndexed { row, column, value ->
            if (value == regionId) {
                rowSum += row
                columnSum += column
                ++numberOfCellsIncluded
            }
        }

        if (numberOfCellsIncluded == 0) {
            return null
        }

        return Pair(rowSum / numberOfCellsIncluded, columnSum / numberOfCellsIncluded)
    }

    fun getPoints() = points.toList()

    class PointDirection(val row: Int, val column: Int, val direction: Direction)

    companion object {
        val backgroundRegion = 0
        val interiorPointRegion = 1
        val borderRegion = 2
        val startRegionCount = 10
    }

}