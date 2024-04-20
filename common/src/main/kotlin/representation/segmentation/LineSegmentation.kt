package com.kjipo.segmentation

import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.EncodingUtilities
import com.kjipo.representation.raster.FlowDirection
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt


fun expandLine(raster: Array<BooleanArray>): List<Pair<Int, Int>> {
    val matrix = Matrix(raster.size, raster[0].size, raster.map { it.toTypedArray() }.toTypedArray())
    val seenCells = Matrix(raster.size, raster[0].size, Array<Array<Boolean>>(raster.size, { Array<Boolean>(raster[0].size, { false }) }))
    var start: Pair<Int, Int>? = null
    var axis: FlowDirection? = null

    while (axis == null) {
        matrix.
                forEachIndexed { x, y, b ->
            if (b && !seenCells[x, y]) {
                start = Pair(x, y)
                seenCells[x, y] = true
                return@forEachIndexed
            }
        }

        if (start == null) {
            return emptyList()
        }

        println("Start: ${start}")

        axis = determineAxis(matrix, start!!)
    }

    print("Axis: ${axis}")

    return expandLineSegment(matrix, start!!, axis)
}


fun expandLineSegment(matrix: Matrix<Boolean>, start: Pair<Int, Int>, flowDirection: FlowDirection): List<Pair<Int, Int>> {
    val segment = mutableListOf<Pair<Int, Int>>()

    segment.add(start)

    var upperEndpoint = start.copy()
    var lowerEndpoint = start.copy()

    var upperChange = true
    var lowerChange = true

    while (lowerChange || upperChange) {
        val upperCandidate = Pair(upperEndpoint.first + flowDirection.rowShift, upperEndpoint.second + flowDirection.columnShift)
        upperEndpoint = if (EncodingUtilities.validCoordinates(upperCandidate.first, upperCandidate.second, matrix.numberOfRows, matrix.numberOfColumns)
                && matrix[upperCandidate.first, upperCandidate.second]) {
            segment.add(upperCandidate)
            upperCandidate
        } else {
            upperChange = false
            upperEndpoint
        }

        val lowerCandidate = Pair(lowerEndpoint.first - flowDirection.rowShift, lowerEndpoint.second - flowDirection.columnShift)
        lowerEndpoint = if (EncodingUtilities.validCoordinates(lowerCandidate.first, lowerCandidate.second, matrix.numberOfRows, matrix.numberOfColumns)
                && matrix[lowerCandidate.first, lowerCandidate.second]) {
            segment.add(lowerCandidate)
            lowerCandidate
        } else {
            lowerChange = false
            lowerEndpoint
        }
    }

    return segment
}

fun determineAxis(matrix: Matrix<Boolean>, start: Pair<Int, Int>): FlowDirection? {
    return FlowDirection.values().asList()
            .find { EncodingUtilities.validCell(start.first, start.second, it, matrix.numberOfRows, matrix.numberOfColumns) && matrix[start.first, start.second] }
}

fun traceSegments(raster: Array<BooleanArray>): List<Pair<Int, Int>> {
    val matrix = Matrix(raster.size, raster[0].size, raster.map { it.toTypedArray() }.toTypedArray())
    return getStartCell(matrix)?.let { traceLineSegment(matrix, it) }.orEmpty()
}

private fun getStartCell(matrix: Matrix<Boolean>): Pair<Int, Int>? {
    matrix.forEachIndexed { x, y, b ->
        if (b) {
            return Pair(x, y)
        }
    }
    return null
}


fun traceLineSegment(matrix: Matrix<Boolean>, start: Pair<Int, Int>): List<Pair<Int, Int>> {
    val segment = mutableListOf<Pair<Int, Int>>()
    val seenCells = Matrix(matrix.numberOfRows, matrix.numberOfColumns,
            Array<Array<Boolean>>(matrix.numberOfRows, { Array<Boolean>(matrix.numberOfColumns, { false }) }))

    var startCell = start

    for (i in 0..20) {

        println("Start cell: $startCell")

        seenCells[startCell.first, startCell.second] = true

        val newSegment = addSingleSegment(matrix, seenCells, startCell)

        println("New segment: $newSegment")

        segment.addAll(newSegment)

        val distanceMap = computeDistanceMap(matrix, segment)

        var startCandidate = Pair(-1, -1)
        var maxDistance = -1

        for (row in 0 until matrix.numberOfRows) {
            for (column in 0 until matrix.numberOfColumns) {
                if (!seenCells[row, column]
                        && matrix[row, column]
                        && maxDistance < distanceMap[row, column]) {
                    startCandidate = Pair(row, column)
                    maxDistance = distanceMap[row, column]
                }
            }
        }

        if (maxDistance == -1) {
            return segment
        }

        startCell = startCandidate
    }

    return segment
}


fun addSingleSegment(matrix: Matrix<Boolean>, seenCells: Matrix<Boolean>, start: Pair<Int, Int>): List<Pair<Int, Int>> {
    val segment = mutableListOf<Pair<Int, Int>>()

    segment.add(start)

    var upperEndpoint = start.copy()
    var lowerEndpoint = start.copy()

    var upperChange = true
    var lowerChange = true

    while (upperChange || lowerChange) {
        upperChange = false
        lowerChange = false

        (0..4).map {
            val upperCandidate = Pair(upperEndpoint.first + FlowDirection.values()[it].rowShift,
                    upperEndpoint.second + FlowDirection.values()[it].columnShift)
            if (EncodingUtilities.validCoordinates(upperCandidate.first, upperCandidate.second,
                            matrix.numberOfRows, matrix.numberOfColumns)
                    && !seenCells[upperCandidate.first, upperCandidate.second]
                    && matrix[upperCandidate.first, upperCandidate.second]) {
                seenCells[upperCandidate.first, upperCandidate.second] = true
                upperChange = true
                upperCandidate
            } else {
                null
            }
        }.filterNotNull().firstOrNull()?.let {
            segment.add(it)
            upperEndpoint = it
        }

        (5..7).map {
            val lowerCandidate = Pair(lowerEndpoint.first + FlowDirection.values()[it].rowShift,
                    lowerEndpoint.second + FlowDirection.values()[it].columnShift)
            if (EncodingUtilities.validCoordinates(lowerCandidate.first, lowerCandidate.second,
                            matrix.numberOfRows, matrix.numberOfColumns)
                    && !seenCells[lowerCandidate.first, lowerCandidate.second]
                    && matrix[lowerCandidate.first, lowerCandidate.second]) {
                seenCells[lowerCandidate.first, lowerCandidate.second] = true
                lowerChange = true
                lowerCandidate
            } else {
                null
            }
        }.filterNotNull().firstOrNull()?.let {
            segment.add(it)
            lowerEndpoint = it
        }
    }

    return segment
}


fun distance(start: Pair<Int, Int>, stop: Pair<Int, Int>) =
        sqrt(abs(stop.first.minus(start.first).toDouble()).pow(2.0)
                + abs(stop.second.minus(start.second).toDouble()).pow(2.0))


fun computeLine(start: Pair<Int, Int>, stop: Pair<Int, Int>): List<Pair<Int, Int>> {
    if (start.first == stop.first) {
        // Horizontal line
        return if (start.second < stop.second) {
            (start.second..(stop.second)).map { Pair(start.first, it) }
        } else {
            (stop.second..(start.second)).map { Pair(start.first, it) }
        }
    }

    if (start.second == stop.second) {
        // Vertical line
        return if (start.first < stop.first) {
            (start.first..(stop.first)).map { Pair(it, start.second) }
        } else {
            (stop.first..(start.first)).map { Pair(it, start.second) }
        }
    }

    val swap = stop.first < start.first

    val firstTranslate = abs(min(0, min(start.first, stop.first)))
    val secondTranslate = abs(min(0, min(start.second, stop.second)))

    val (startPair, stopPair) = if (swap) {
        Pair(Pair(stop.first + firstTranslate, stop.second + secondTranslate),
                Pair(start.first + firstTranslate, start.second + secondTranslate))
    } else {
        Pair(Pair(start.first + firstTranslate, start.second + secondTranslate),
                Pair(stop.first + firstTranslate, stop.second + secondTranslate))
    }

    val xDelta = stopPair.first.minus(startPair.first).toDouble()
    val yDelta = stopPair.second.minus(startPair.second).toDouble()
    val deltaError = abs(yDelta / xDelta)
    val signYDelta = if (yDelta < 0) -1 else 1


    var error = 0.0 //deltaError - 0.5
    var y = startPair.second

    val segment = mutableListOf<Pair<Int, Int>>()

    var newY = y
    for (x in startPair.first..stopPair.first) {
        if (y != newY) {
            if (signYDelta < 0) {
                for (incY in (newY..y)) {
                    segment.add(Pair(x, incY))
                }
            } else {
                for (incY in (y..newY)) {
                    segment.add(Pair(x, incY))
                }
            }
        } else {
            segment.add(Pair(x, y))
        }
        y = newY

        error += deltaError
        while (error >= 0.5) {
            newY += signYDelta
            error -= 1.0
        }
    }

    return if (swap) {
        segment.map { Pair(it.first - firstTranslate, it.second - secondTranslate) }.reversed()
    } else {
        segment.map { Pair(it.first - firstTranslate, it.second - secondTranslate) }
    }
}


fun computeSegmentScore(matrix: Matrix<Boolean>, segment: List<Pair<Int, Int>>): Int {
    return segment.map {
        if (matrix[it.first, it.second]) {
            1
        } else {
            0
        }
    }.sum()
}


fun computeDistanceMap(matrix: Matrix<Boolean>, segment: List<Pair<Int, Int>>): Matrix<Int> {
    val distanceMap = Matrix(matrix.numberOfRows, matrix.numberOfColumns,
            Array<Array<Int>>(matrix.numberOfRows, { Array<Int>(matrix.numberOfColumns, { Int.MIN_VALUE }) }))

    var cellsToProcessNext = mutableSetOf<Pair<Int, Int>>()
    cellsToProcessNext.addAll(segment)

    var distance = 0

    while (cellsToProcessNext.isNotEmpty()) {
        cellsToProcessNext = cellsToProcessNext.map {
            distanceMap[it.first, it.second] = distance
            val currentCell = it

            FlowDirection.values()
                    .map { Pair(currentCell.first + it.rowShift, currentCell.second + it.columnShift) }
                    .filter { EncodingUtilities.validCoordinates(it.first, it.second, matrix.numberOfRows, matrix.numberOfColumns) }
                    .filter { distanceMap[it.first, it.second] == Int.MIN_VALUE }
        }.flatten().toMutableSet()

        ++distance
    }

    return distanceMap
}

