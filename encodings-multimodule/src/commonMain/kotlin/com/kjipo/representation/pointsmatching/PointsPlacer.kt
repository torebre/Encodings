package com.kjipo.representation.pointsmatching

import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.FlowDirection
import com.kjipo.representation.raster.getFlowDirectionForOffset
import kotlin.math.pow
import kotlin.math.sqrt

class PointsPlacer(private val imageMatrix: Matrix<Boolean>) {
    private val points = mutableListOf<Pair<Int, Int>>()


    fun runPlacement() {
        val firstPoint = addFirstPoint()
        points.add(firstPoint)

        addPoint(firstPoint)?.let {
            points.add(it)
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
                temp = Pair(row, column)
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

            println("Current point: ${currentPoint}. Updated point: ${updatedPoint}")

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
            if (value != null) {
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


    fun getPoints() = points.toList()


}