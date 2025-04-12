package com.kjipo.experiments

import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.FlowDirection
import com.kjipo.representation.raster.getFlowDirectionForOffset
import com.kjipo.representation.raster.getNeighbourhood
import representation.identifyRegions
import kotlin.math.absoluteValue


class BallRoller {

    private val circleMaskCache: Map<Int, CircleMaskInformation> = mutableMapOf()


    fun extractStrokes(kanjiImage: Matrix<Boolean>): List<Stroke> {
//        val regions = identifyRegions(kanjiImage, 1)
        val gradientImage = generateGradientImage(kanjiImage)
        val strokes = mutableListOf<Stroke>()

        val usedPointsImage = Matrix(
            kanjiImage.numberOfRows, kanjiImage.numberOfColumns,
            { row, column ->
                // If the point is outside the figure, it counts as
                // being a used point
                !kanjiImage[row, column]
            })


        var counter = 0



        for (row in 0 until kanjiImage.numberOfRows) {
            for (column in 0 until kanjiImage.numberOfColumns) {
                if (!usedPointsImage[row, column]) {
                    val path = generatePath(row, column, gradientImage, usedPointsImage)

                    val regionMatrix = identifyRegions(kanjiImage)
//                    val path = generatePath2(row, column, gradientImage, kanjiImage, regionMatrix)
//                    if (path.path.isNotEmpty()) {
//                        strokes.add(path)
//                    }

                    ++counter

                    // TODO Limit the number of strokes returned while testing
                    if (counter == 20) {
                        return strokes
                    }


                    // TODO


                }

            }

        }

        return strokes
    }


    fun extractStrokes2(kanjiImage: Matrix<Boolean>): Matrix<Int> {
//        val regions = identifyRegions(kanjiImage, 1)
        val gradientImage = generateGradientImage(kanjiImage)
//        val strokes = mutableListOf<Stroke>()

        val usedPointsImage = Matrix(
            kanjiImage.numberOfRows, kanjiImage.numberOfColumns,
            { row, column ->
                // If the point is outside the figure, it counts as
                // being a used point
                !kanjiImage[row, column]
            })

        var counter = 0

        val regionMatrix = identifyRegions(kanjiImage)
        val updatedKanjiImage = Matrix.copy(kanjiImage)

        for (row in 0 until kanjiImage.numberOfRows) {
            for (column in 0 until kanjiImage.numberOfColumns) {
//                if (!usedPointsImage[row, column]) {
                if (updatedKanjiImage[row, column]) {
//                    val path = generatePath(row, column, gradientImage, usedPointsImage)

                    generatePath2(
                        row, column,
                        gradientImage,
                        kanjiImage,
                        regionMatrix,
                        updatedKanjiImage
                    )

                    ++counter

                    // TODO Limit the number of strokes returned while testing
                    if (counter == 20) {
//                        return strokes
                        return colorImage(kanjiImage, updatedKanjiImage)
                    }


                    // TODO


                }

            }

        }

        return colorImage(kanjiImage, updatedKanjiImage)
    }


    fun addCircle(kanjiImage: Matrix<Boolean>): Matrix<Int> {
        val examinedPoints = Matrix(
            kanjiImage.numberOfRows, kanjiImage.numberOfColumns,
            { row, column ->
                // If the point is outside the figure, it counts as
                // being an examined point
                !kanjiImage[row, column]
            })

        val circleMatrix: Matrix<CircleMaskInformation?> = Matrix(
            kanjiImage.numberOfRows,
            kanjiImage.numberOfColumns,
            { _, _ -> null })

        val updatedKanjiImage = Matrix(kanjiImage.numberOfRows, kanjiImage.numberOfColumns, { row, column ->
            if (kanjiImage[row, column]) {
                1
            } else {
                0
            }
        })

        for (row in 0 until kanjiImage.numberOfRows) {
            for (column in 0 until kanjiImage.numberOfColumns) {
                if (kanjiImage[row, column]) {
                    val largestCircle = getLargestCircle(row, column, kanjiImage)

//                    println("Point: $row, $column. Largest circle: ${largestCircle.radius}")

                    circleMatrix[row, column] = largestCircle
                }
            }
        }

        var largestCirclePoint = Pair(0, 0)
        var largestCircle = CircleMaskInformation(0, Matrix(1, 1, { _, _ -> true }), 1)

        for (row in 0 until kanjiImage.numberOfRows) {
            for (column in 0 until kanjiImage.numberOfColumns) {
                val circle = circleMatrix[row, column]

                if (circle != null) {
                    if (circle.radius > largestCircle.radius) {
                        largestCircle = circle
                        largestCirclePoint = Pair(row, column)
                    }
                }
            }
        }

        println("Test50: Largest circle: ${largestCircle.radius}")

        val circleList = mutableListOf<Pair<Pair<Int, Int>, CircleMaskInformation>>()
        for (row in 0 until kanjiImage.numberOfRows) {
            for (column in 0 until kanjiImage.numberOfColumns) {
                val circle = circleMatrix[row, column]

                if (circle != null) {
                    circleList.add(Pair(Pair(row, column), circle))
                }
            }
        }

        // TODO The filter is only here to see where the larger circles are
        val sortedCircleList = circleList
            .filter { it.second.radius > 2 }
            .sortedByDescending { it.second.radius }

        var counter = 0
        for (row in 0 until kanjiImage.numberOfRows) {
            for (column in 0 until kanjiImage.numberOfColumns) {
                if (circleMatrix[row, column] != null) {
                    ++counter
                }
            }
        }

        println("Test31: Number of circles: $counter")

        for (pointCircleInfoPair in sortedCircleList) {

            println("Test60: ${pointCircleInfoPair.second.radius}")

//            if (circleMatrix[pointCircleInfoPair.first.first, pointCircleInfoPair.first.second] == null) {
//                continue
//            }
//
//            applyCircleMask(
//                pointCircleInfoPair.first.first,
//                pointCircleInfoPair.first.second,
//                circleMatrix,
//                pointCircleInfoPair.second.circleMask,
//                { row, column ->
//                    if (pointCircleInfoPair.first.first == row && pointCircleInfoPair.first.second == column) {
//                        circleMatrix[row, column]
//
//                    } else {
//                        null
//                    }
//                })

            applyCircleMask(
                pointCircleInfoPair.first.first,
                pointCircleInfoPair.first.second,
                updatedKanjiImage,
                pointCircleInfoPair.second.circleMask,
                { row, column ->
                    2
                })
        }

        counter = 0
        for (row in 0 until kanjiImage.numberOfRows) {
            for (column in 0 until kanjiImage.numberOfColumns) {
                if (circleMatrix[row, column] != null) {
                    ++counter
                }
            }
        }
        println("Test32: Number of circles: $counter")


//        for (row in 0 until kanjiImage.numberOfRows) {
//            for (column in 0 until kanjiImage.numberOfColumns) {
//                val circle = circleMatrix[row, column]
//                if (circle != null) {
//                    applyCircleMask(
//                        row, column,
//                        updatedKanjiImage, circle.circleMask, { row, column ->
//                            2
//                        })
//                }
//            }
//        }

        return updatedKanjiImage
    }


    fun createPathFromCircle(kanjiImage: Matrix<Boolean>): Matrix<Int> {
        val circleMatrix: Matrix<CircleMaskInformation?> = Matrix(
            kanjiImage.numberOfRows,
            kanjiImage.numberOfColumns,
            { _, _ -> null })


        for (row in 0 until kanjiImage.numberOfRows) {
            for (column in 0 until kanjiImage.numberOfColumns) {
                if (kanjiImage[row, column]) {
                    val largestCircle = getLargestCircle(row, column, kanjiImage)

//                    println("Point: $row, $column. Largest circle: ${largestCircle.radius}")

                    circleMatrix[row, column] = largestCircle
                }
            }
        }

        var largestCirclePoint = Point(0, 0)
        var largestCircle = CircleMaskInformation(0, Matrix(1, 1, { _, _ -> true }), 1)

        for (row in 0 until kanjiImage.numberOfRows) {
            for (column in 0 until kanjiImage.numberOfColumns) {
                val circle = circleMatrix[row, column]

                if (circle != null) {
                    if (circle.radius > largestCircle.radius) {
                        largestCircle = circle
                        largestCirclePoint = Point(row, column)
                    }
                }
            }
        }

        println("Test50: Largest circle: ${largestCircle.radius}")

        val usedPointsImage = Matrix(
            kanjiImage.numberOfRows, kanjiImage.numberOfColumns,
            { row, column ->
                // If the point is outside the figure, it counts as
                // being an examined point
                kanjiImage[row, column]
            })

        val path = mutableListOf<CirclePathStep>()

        var currentCircleCenter = largestCirclePoint
        var updatedCircle = moveCircle(largestCirclePoint, usedPointsImage, kanjiImage)
        path.add(CirclePathStep(largestCirclePoint, largestCircle))

        while (updatedCircle !== null) {
            applyCircleMask(
                currentCircleCenter.row,
                currentCircleCenter.column,
                usedPointsImage,
                updatedCircle.second.circleMask,
                { _, _ -> false })
            currentCircleCenter = Point(
                currentCircleCenter.row + updatedCircle.first.rowShift,
                currentCircleCenter.column + updatedCircle.first.columnShift
            )
            path.add(CirclePathStep(currentCircleCenter, updatedCircle.second))
            updatedCircle = moveCircle(currentCircleCenter, usedPointsImage, kanjiImage)
        }

        print("Test60: Path length: ${path.size}")

        val updatedKanjiImage = Matrix(kanjiImage.numberOfRows, kanjiImage.numberOfColumns, { row, column ->
            if (kanjiImage[row, column]) {
                1
            } else {
                0
            }
        })

        for (circlePathStep in path) {
            applyCircleMask(
                circlePathStep.circleCenter.row,
                circlePathStep.circleCenter.column,
                updatedKanjiImage,
                circlePathStep.circleMaskInformation.circleMask,
                { row, column ->
                    2
                })
        }

        return updatedKanjiImage
    }


    private fun getLargestCircle(row: Int, column: Int, kanjiImage: Matrix<Boolean>): CircleMaskInformation {
        var radius = 3
        var circleMask = getCachedCircleMask(radius)

        var pointsWronglyCovered = determinePointsWronglyCovered(row, column, kanjiImage, circleMask.circleMask, false)

        if (pointsWronglyCovered > 0) {
            do {
                --radius
                circleMask = getCachedCircleMask(radius)

                pointsWronglyCovered =
                    determinePointsWronglyCovered(row, column, kanjiImage, circleMask.circleMask, false)
            } while (pointsWronglyCovered > 0)
        } else if (pointsWronglyCovered == 0) {
            var largestCircle: CircleMaskInformation

            do {
                largestCircle = circleMask
                ++radius
                circleMask = getCachedCircleMask(radius)

                pointsWronglyCovered =
                    determinePointsWronglyCovered(row, column, kanjiImage, circleMask.circleMask, false)
            } while (pointsWronglyCovered == 0)
            return largestCircle
        }

        return circleMask
    }

    private fun moveCircle(
        circleCenter: Point,
        usedPointsImage: Matrix<Boolean>,
        orignalKanjiImage: Matrix<Boolean>
    ): Pair<FlowDirection, CircleMaskInformation>? {
        return moveCircle(
            circleCenter,
            usedPointsImage,
            getCirclesAppliedAtPoints(
                determinePointsToApplyCircleTo(circleCenter, orignalKanjiImage),
                orignalKanjiImage
            )
        )
    }


    private fun moveCircle(
        circleCenter: Point,
        usedPointsImage: Matrix<Boolean>,
        circlesAppliedAtPoints: Map<Point, CircleMaskInformation>
    ): Pair<FlowDirection, CircleMaskInformation>? {
        var selectedDirection = FlowDirection.EAST
        val matrix = Matrix(0, 0, arrayOf(arrayOf(false)))
        var selectedCircleMaskInformation = CircleMaskInformation(0, matrix, 0)

        var maxPointsCovered = 0
        for (circlePoint in circlesAppliedAtPoints) {
            // Assume that the new circle points and the original one are neighbours
            val flowDirection = getFlowDirectionForOffset(
                circlePoint.key.row - circleCenter.row,
                circlePoint.key.column - circleCenter.column
            )

            if (flowDirection == null) {
                continue
            }

            val pointsCovered = determinePointsCovered(
                circleCenter.row + flowDirection.rowShift,
                circleCenter.column + flowDirection.columnShift,
                usedPointsImage,
                circlePoint.value.circleMask
            )

            if (pointsCovered > maxPointsCovered) {
                selectedDirection = flowDirection
                maxPointsCovered = pointsCovered
                selectedCircleMaskInformation = circlePoint.value
            }
        }

        if (maxPointsCovered == 0) {
            return null
        }

        return Pair(selectedDirection, selectedCircleMaskInformation)
    }

    private fun determinePointsToApplyCircleTo(
        circleCenter: Point,
        orignalKanjiImage: Matrix<Boolean>
    ): List<Point> {
        val neighbourhood = getNeighbourhood(orignalKanjiImage, circleCenter.row, circleCenter.column)

        return FlowDirection.entries.mapNotNull { flowDirection ->
            if (neighbourhood[1 + flowDirection.rowShift, 1 + flowDirection.columnShift]
                && orignalKanjiImage[circleCenter.row + flowDirection.rowShift, circleCenter.column + flowDirection.columnShift]
            ) {
                Point(circleCenter.row + flowDirection.rowShift, circleCenter.column + flowDirection.columnShift)
            } else {
                null
            }
        }
            .toList()
    }

    private fun getCirclesAppliedAtPoints(
        pointsToApplyCirclesTo: Iterable<Point>,
        orignalKanjiImage: Matrix<Boolean>
    ): Map<Point, CircleMaskInformation> {
        val directionCircleMap = mutableMapOf<Point, CircleMaskInformation>()
        pointsToApplyCirclesTo.map { point ->
            directionCircleMap[point] = getLargestCircle(
                point.row,
                point.column,
                orignalKanjiImage
            )
        }

        return directionCircleMap
    }


    private fun determinePointsCovered(
        row: Int,
        column: Int,
        updatedKanjiImage: Matrix<Boolean>,
        circleMask: Matrix<Boolean>,
    ): Int {
        return determinePointsCovered(row, column, updatedKanjiImage, circleMask, true)
    }

    private fun <T> determinePointsCovered(
        row: Int,
        column: Int,
        updatedKanjiImage: Matrix<T>,
        circleMask: Matrix<Boolean>,
        correctPointValue: T
    ): Int {
        var pointsCovered = 0

        applyCircleMask(row, column, updatedKanjiImage, circleMask, { rowInImage, columnInImage ->
            if (updatedKanjiImage[rowInImage, columnInImage] == correctPointValue) {
                ++pointsCovered
            }

            // Just return the original value
            updatedKanjiImage[rowInImage, columnInImage]
        })

        return pointsCovered
    }

    private fun <T> determinePointsWronglyCovered(
        row: Int,
        column: Int,
        updatedKanjiImage: Matrix<T>,
        circleMask: Matrix<Boolean>,
        wrongPointValue: T
    ): Int {
        var pointsWronglyCovered = 0

        applyCircleMask(row, column, updatedKanjiImage, circleMask, { rowInImage, columnInImage ->
            if (updatedKanjiImage[rowInImage, columnInImage] == wrongPointValue) {
                ++pointsWronglyCovered
            }

            // Just return the original value
            updatedKanjiImage[rowInImage, columnInImage]
        })

        return pointsWronglyCovered
    }

    private fun colorImage(originalImage: Matrix<Boolean>, updateImage: Matrix<Boolean>): Matrix<Int> {
        return Matrix(updateImage.numberOfRows, updateImage.numberOfColumns, { row, column ->
            if (originalImage[row, column] && updateImage[row, column]) {
                1
            } else if (originalImage[row, column] && !updateImage[row, column]) {
                2
            } else {
                0
            }
        })
    }

    private fun generatePath(
        row: Int,
        column: Int,
        gradientImage: Matrix<Int>,
        usedPointsImage: Matrix<Boolean>
    ): Stroke {
        val neighbourhood = getNeighbourhood(gradientImage, row, column)
        val path = mutableListOf<PathPoint>()

        val pointsToExamine = mutableListOf<Pair<Int, Int>>()
        pointsToExamine.add(Pair(row, column))

        while (pointsToExamine.isNotEmpty()) {
            var currentPoint = pointsToExamine.removeFirst()
            usedPointsImage[currentPoint.first, currentPoint.second] = true
//            path.add(currentPoint)

            var maxDistanceFromEdge = 0
            val maxDistancePoints = mutableListOf<FlowDirection>()

            for (pair in neighbourhood) {
                if (pair.second) {
                    val direction = pair.first

                    val rowNeighbour = currentPoint.first + direction.rowShift
                    val columnNeighbour = currentPoint.second + direction.columnShift

                    if (usedPointsImage[rowNeighbour, columnNeighbour]) {
                        continue
                    }

                    val distanceFromEdge = gradientImage[rowNeighbour, columnNeighbour]

                    if (distanceFromEdge > maxDistanceFromEdge) {
                        maxDistanceFromEdge = distanceFromEdge
                        maxDistancePoints.clear()
                        maxDistancePoints.add(direction)
                    } else if (distanceFromEdge == maxDistanceFromEdge) {
                        maxDistancePoints.add(direction)
                    }
                }
            }

            for (pair in neighbourhood) {
                if (pair.second) {
                    val direction = pair.first
                    val rowNeighbour = currentPoint.first + direction.rowShift
                    val columnNeighbour = currentPoint.second + direction.columnShift

                    usedPointsImage[rowNeighbour, columnNeighbour] = true
                }
            }

            if (maxDistanceFromEdge == 0) {
                continue
            }


            for (maxDistancePoint in maxDistancePoints) {
                val row = currentPoint.first + maxDistancePoint.rowShift
                val column = currentPoint.second + maxDistancePoint.columnShift

                path.add(PathPoint.PathPointWithDirection(row, column, maxDistancePoint))
                pointsToExamine.add(Pair(row, column))
            }
        }

        return Stroke(path)
    }


    private fun fitForMask(row: Int, column: Int, kanjiImage: Matrix<Boolean>): Int {
        val neighbourhood = getNeighbourhood(kanjiImage, row, column)

        var coveredPoints = 0
        for (entry in FlowDirection.entries) {
            if (neighbourhood[row + entry.rowShift, column + entry.columnShift]
                && kanjiImage[entry.rowShift, entry.columnShift]
            ) {
                ++coveredPoints
            }
        }

        return coveredPoints
    }

    private fun generatePath2(
        row: Int,
        column: Int,
        gradientImage: Matrix<Int>,
        kanjiImage: Matrix<Boolean>,
        regionImage: Matrix<Int>,
        imageToWriteTo: Matrix<Boolean>
    ) {
        val circleMask = determineCircleMask(50)

//        val neighbourhood = getNeighbourhood(gradientImage, row, column)
//        val path = mutableListOf<PathPoint>()

        val pointsToExamine = mutableListOf<Pair<Int, Int>>()
        pointsToExamine.add(Pair(row, column))

        val currentRegion = regionImage[row, column]
        val circleSegments = mutableListOf<Pair<Int, Int>>()


        while (pointsToExamine.isNotEmpty()) {
            var currentPoint = pointsToExamine.removeFirst()

            applyCircleMask(
                currentPoint.first,
                currentPoint.second,
                gradientImage,
                circleMask,
                { rowCircle, columnCircle ->
                    if (regionImage[rowCircle, columnCircle] == currentRegion) {
                        circleSegments.add(Pair(row, column))
                    }
                    // Just return the original value to avoid changing the gradientImage matrix given as input to applyCircleMask
                    gradientImage[rowCircle, columnCircle]
                }
            )

            val distinctSegments = mutableListOf<List<Pair<Int, Int>>>()
            var segment = mutableListOf<Pair<Int, Int>>()
            for (point in circleSegments) {
                if (segment.isEmpty()) {
                    segment.add(Pair(row, column))
                } else {
                    if (areNeighbours(point, segment.last())) {
                        segment.add(point)
                    } else {
                        distinctSegments.add(segment)
                        segment = mutableListOf()
                    }
                }
            }
            distinctSegments.add(segment)

            print("Number of segments: ${distinctSegments.size}")

            for (distinctSegment in distinctSegments) {
//                val midpoint = distinctSegment[distinctSegment.size / 2]
//                gradientImage[midpoint.first, midpoint.second]

                for (pair in distinctSegment) {
                    imageToWriteTo[pair.first, pair.second] = false
                }

            }

            removeInteriorPoints(currentPoint, imageToWriteTo, false)
        }

//        return Stroke(path)

    }

    private fun areNeighbours(point: Pair<Int, Int>, last: Pair<Int, Int>): Boolean {
        return (point.first - last.first).absoluteValue < 2
                && (point.second - last.second).absoluteValue < 2
    }

    private fun getCachedCircleMask(radius: Int): CircleMaskInformation {
        val circleMask = determineCircleMask(radius)

        var sizeOfMask = 0
        circleMask.forEach { value ->
            if (value) {
                ++sizeOfMask
            }
        }

        return CircleMaskInformation(radius, circleMask, sizeOfMask)
    }


    private class CircleMaskInformation(val radius: Int, val circleMask: Matrix<Boolean>, val sizeOfMask: Int)

    private data class CirclePathStep(
        val circleCenter: Point,
        val circleMaskInformation: CircleMaskInformation
    )

}