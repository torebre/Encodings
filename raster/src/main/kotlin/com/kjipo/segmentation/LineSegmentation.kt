package com.kjipo.segmentation

import com.kjipo.raster.EncodingUtilities
import com.kjipo.raster.FlowDirection


fun expandLine(raster: Array<BooleanArray>): List<Pair<Int, Int>> {
    val matrix = Matrix(raster.size, raster[0].size, raster.map { it.toTypedArray() }.toTypedArray())
    val seenCells = Matrix(raster.size, raster[0].size, Array<Array<Boolean>>(raster.size, { Array<Boolean>(raster[0].size, { false }) }))
    var start: Pair<Int, Int>? = null
    var axis: FlowDirection? = null

    while (axis == null) {
        matrix.forEachIndexed { x, y, b ->
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
    val seenCells = Matrix(raster.size, raster[0].size, Array<Array<Boolean>>(raster.size, { Array<Boolean>(raster[0].size, { false }) }))
    var start: Pair<Int, Int>? = null

    matrix.forEachIndexed { x, y, b ->
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

    return traceLineSegment(matrix, start!!)
}


fun traceLineSegment(matrix: Matrix<Boolean>, start: Pair<Int, Int>): List<Pair<Int, Int>> {
    val segment = mutableListOf<Pair<Int, Int>>()
    val seenCells = Matrix(matrix.numberOfRows, matrix.numberOfColumns,
            Array<Array<Boolean>>(matrix.numberOfRows, { Array<Boolean>(matrix.numberOfColumns, { false }) }))

    segment.add(start)

    var upperEndpoint = start.copy()
    var lowerEndpoint = start.copy()

    var upperChange = true
    var lowerChange = true

    while(upperChange || lowerChange) {
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
            upperEndpoint = it }

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
            lowerEndpoint = it }

        println("Upper: $upperEndpoint. Lower: $lowerEndpoint")
    }

    val distance = distance(lowerEndpoint, upperEndpoint)

    println("Distance: ${distance}")

    return segment
}


fun distance(start: Pair<Int, Int>, stop: Pair<Int, Int>) =
        Math.sqrt(Math.pow(Math.abs(stop.first.minus(start.first).toDouble()), 2.0)
                + Math.pow(Math.abs(stop.second.minus(start.second).toDouble()), 2.0))


//function line(x0, y0, x1, y1)
//real deltax := x1 - x0
//real deltay := y1 - y0
//real deltaerr := abs(deltay / deltax)    // Assume deltax != 0 (line is not vertical),
//// note that this division needs to be done in a way that preserves the fractional part
//real error := deltaerr - 0.5
//int y := y0
//for x from x0 to x1
//plot(x,y)
//error := error + deltaerr
//if error ≥ 0.5 then
//y := y + 1
//error := error - 1.0


fun computeLine(start: Pair<Int, Int>, stop: Pair<Int, Int>): List<Pair<Int, Int>> {
    if (start.first == stop.first) {
        // Vertical line
        return (start.second..stop.second).map { Pair(start.first, it) }
    }

    val xDelta = stop.first.minus(start.first).toDouble()
    val yDelta = stop.second.minus(start.second).toDouble()
    val deltaError = Math.abs(yDelta / xDelta)

    var error = deltaError - 0.5
    var y = start.second

    val segment = mutableListOf<Pair<Int, Int>>()

    for (x in start.first..stop.first) {
        segment.add(Pair(x, y))
        error += deltaError
        if (error >= 0.5) {
            y += 1
            error -= 1.0
        }
    }

    return segment
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


