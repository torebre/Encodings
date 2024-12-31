package com.kjipo.experiments

import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.FlowDirection
import com.kjipo.representation.raster.getNeighbourhood
import com.kjipo.segmentation.getOffset
import representation.identifyRegions
import kotlin.math.absoluteValue
import kotlin.math.max


class BallRoller {


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

//        val updatedKanjiImage = Matrix.copy(kanjiImage)
        val updatedKanjiImage = Matrix(kanjiImage.numberOfRows, kanjiImage.numberOfColumns, { row, column ->
            if (kanjiImage[row, column]) {
                1
            } else {
                0
            }
        })

        var radius = 5
        val circleMask = determineCircleMask(radius)

        var sizeOfMask = 0
        circleMask.forEach { value ->
            if (value) {
                ++sizeOfMask
            }
        }

        for (row in 0 until kanjiImage.numberOfRows) {
            for (column in 0 until kanjiImage.numberOfColumns) {
//                if (!usedPointsImage[row, column]) {
                if (updatedKanjiImage[row, column] == 1) {
//                    val path = generatePath(row, column, gradientImage, usedPointsImage)

                    // TODO

//                    applyCircleMask(
//                        row,
//                        column,
//                        matrix,
//                        circleMask,
//                        valueFunction)


                    var pointsCovered = determinePointsCovered(row, column, updatedKanjiImage, circleMask)
                    var pointsWronglyCovered = determinePointsWronglyCovered(row, column, updatedKanjiImage, circleMask)

                    var maxPoint = Pair(row, column)
                    var maxPointsCovered = pointsCovered
                    val neighbourhood = getNeighbourhood(updatedKanjiImage, row, column)
                    neighbourhood.forEach { directionValidPair ->
                        val direction = directionValidPair.first
                        val validValue = directionValidPair.second

                        if (validValue) {
                            val covered = determinePointsCovered(row + direction.rowShift, column + direction.columnShift, updatedKanjiImage, circleMask)
                            val wronglyCovered = determinePointsWronglyCovered(row + direction.rowShift, column + direction.columnShift, updatedKanjiImage, circleMask)

                            if(covered >= maxPointsCovered) {
                                println("Test23")

                                if(wronglyCovered == 0) {
                                    println("Test24")

                                    maxPoint = Pair(row + direction.rowShift, column + direction.columnShift)
                                    maxPointsCovered = covered
                                }

                            }

                        }
                    }

                    applyCircleMask(maxPoint.first, maxPoint.second,
                        updatedKanjiImage, circleMask, {row, column ->
                           2
                        })

                    return updatedKanjiImage


//                    generatePath2(
//                        row, column,
//                        gradientImage,
//                        kanjiImage,
//                        regionMatrix,
//                        updatedKanjiImage
//                    )

                    ++counter

                    // TODO Limit the number of strokes returned while testing
//                    if (counter == 20) {
////                        return strokes
////                        return colorImage(kanjiImage, updatedKanjiImage)
//                        return updatedKanjiImage
//                    }


                    // TODO


                }

            }

        }

//        return colorImage(kanjiImage, updatedKanjiImage)

        return updatedKanjiImage
    }


    private fun determinePointsCovered(row: Int, column: Int, updatedKanjiImage: Matrix<Int>, circleMask: Matrix<Boolean>): Int {
        var pointsCovered = 0

        applyCircleMask(row, column, updatedKanjiImage, circleMask, { rowInImage, columnInImage ->
            if (updatedKanjiImage[rowInImage, columnInImage] == 1) {
                ++pointsCovered
            }

            // Just return the original value
            updatedKanjiImage[rowInImage, columnInImage]
        })

        return pointsCovered
    }

    private fun determinePointsWronglyCovered(row: Int, column: Int, updatedKanjiImage: Matrix<Int>, circleMask: Matrix<Boolean>): Int {
        var pointsWronglyCovered = 0

        applyCircleMask(row, column, updatedKanjiImage, circleMask, { rowInImage, columnInImage ->
            if(updatedKanjiImage[rowInImage, columnInImage] == 0) {
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


}