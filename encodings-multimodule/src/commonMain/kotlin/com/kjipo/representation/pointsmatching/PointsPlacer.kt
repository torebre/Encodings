package com.kjipo.representation.pointsmatching

import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.EncodingUtilities
import com.kjipo.representation.raster.FlowDirection
import com.kjipo.representation.raster.getFlowDirectionForOffset
import com.kjipo.representation.raster.shiftTwoStepsForward
import kotlin.math.pow
import kotlin.math.sqrt

class PointsPlacer(private val imageMatrix: Matrix<Boolean>) {
    private val points = mutableListOf<Pair<Int, Int>>()

    val regionMatrix: Matrix<Int>

    val linePointMatrix: Matrix<Double> by lazy {
        identifyLinePoints()
    }

    val centerOfMassMatrix: Matrix<Int> by lazy {
        centerOfMassPointsForRegions()
    }


    init {
        regionMatrix = identifyRegions()
    }


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

    companion object {
        val backgroundRegion = 0
        val interiorPointRegion = 1
        val startRegionCount = 10
    }

}