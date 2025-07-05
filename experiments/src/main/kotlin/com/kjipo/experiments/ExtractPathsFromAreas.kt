package com.kjipo.experiments

import com.kjipo.representation.LineUtilities.createLine
import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.getNeighbourhood
import com.kjipo.segmentation.getOffset
import representation.identifyRegions
import kotlin.Boolean


class ExtractPathsFromAreas(
    private val areaExtracts: List<AreaExtract>,
    private val imageMatrix: Matrix<Boolean>,
    private val regionMatrix: Matrix<Int>
) {

    private val startSegmentId = 10


    constructor(circlePaths: Iterable<CirclePath>, imageMatrix: Matrix<Boolean>) : this(
        createAreaExtracts(circlePaths),
        imageMatrix,
        identifyRegions(imageMatrix)
    )


    private fun getDistanceMatrix(): Matrix<Int> {
        val distanceMatrix = Matrix(areaExtracts.size, areaExtracts.size, { row, column ->
            Int.MAX_VALUE
        })

        var rowCounter = 0
        for (extract in areaExtracts) {
            var columnCounter = 0

            for (areaExtract in areaExtracts) {
                if (rowCounter == columnCounter) {
                    ++columnCounter
                    continue
                }

                if (regionMatrix[extract.center.row, extract.center.column] != regionMatrix[areaExtract.center.row, areaExtract.center.column]) {
                    ++columnCounter
                    continue
                }

                areaExtract.points.forEach {
                    if (distanceMatrix[rowCounter, columnCounter] == Int.MAX_VALUE) {
                        val straightLineLength = createLine(
                            extract.center.row, extract.center.column,
                            it.row, it.column
                        )
                        distanceMatrix[rowCounter, columnCounter] = straightLineLength.size
                        distanceMatrix[columnCounter, rowCounter] = straightLineLength.size
                    }
                }
                ++columnCounter

            }
            ++rowCounter
        }

        return distanceMatrix
    }


    fun createPathImage(): Matrix<Int> {
        val distanceMatrix = getDistanceMatrix()

        val pathMatrix = Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns, { row, column ->
            if (imageMatrix[row, column]) {
                1
            } else {
                0
            }
        })

        var counter = 0
        var pathCounter = 2

        for (extract in areaExtracts) {
            var counter2 = 0
            var minDistance = Int.MAX_VALUE
            var minDistanceIndex = counter


            for (extract2 in areaExtracts) {
                if (counter == counter2) {
                    ++counter2
                    continue
                }
                if (distanceMatrix[counter, counter2] < minDistance) {
                    minDistance = distanceMatrix[counter, counter2]
                    minDistanceIndex = counter2
                }
                ++counter2
            }

            val straightLineLength = createLine(
                extract.center.row, extract.center.column,
                areaExtracts[minDistanceIndex].center.row, areaExtracts[minDistanceIndex].center.column
            )
            for (pair in straightLineLength) {
                pathMatrix[pair.first, pair.second] = pathCounter
            }

            ++pathCounter
            ++counter
        }

        return pathMatrix
    }


    fun getPathSegments(): List<LineSegment> {
        val distanceMatrix = getDistanceMatrix()
        val lineSegments = mutableListOf<LineSegment>()
        var counter = 0

        for (extract in areaExtracts) {
            var counter2 = 0
            var minDistance = Int.MAX_VALUE
            var minDistanceIndex = counter


            for (extract2 in areaExtracts) {
                if (counter == counter2) {
                    ++counter2
                    continue
                }
                if (distanceMatrix[counter, counter2] < minDistance) {
                    minDistance = distanceMatrix[counter, counter2]
                    minDistanceIndex = counter2
                }
                ++counter2
            }

            val straightLineLength = createLine(
                extract.center.row, extract.center.column,
                areaExtracts[minDistanceIndex].center.row, areaExtracts[minDistanceIndex].center.column
            )

            lineSegments.add(LineSegment(startSegmentId + counter, straightLineLength))
            ++counter
        }

        return lineSegments
    }

    fun joinSegments(): Matrix<Int> {
        val pathSegments = getPathSegments()

        val pathMatrix = Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns, { row, column ->
            if (imageMatrix[row, column]) {
                1
            } else {
                0
            }
        })

        for (segment in pathSegments) {
            for (pair in segment.straightLineLength) {
                pathMatrix[pair.first, pair.second] = segment.id
            }
        }

        return joinSegments(pathSegments, imageMatrix, regionMatrix, pathMatrix)
    }


    fun joinSegments(
        lineSegments: List<LineSegment>,
        imageMatrix: Matrix<Boolean>,
        regionMatrix: Matrix<Int>,
        lineSegmentMatrix: Matrix<Int>
    ): Matrix<Int> {
        for (segment in lineSegments) {
            // TODO Only look at lines longer than 6 pixels to cut down on number of lines to examine while developing
            if (segment.straightLineLength.size < 6) {
                continue
            }

            val closestNeighbours = findClosestNeighboursForSegment(segment, imageMatrix, lineSegmentMatrix)
            examineSegments(segment, lineSegments.filter { closestNeighbours.contains(it.id) })

            // TODO Only look at one segment while developing
            val testMatrix = Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns, { row, column ->
                if (imageMatrix[row, column]) {
                    1
                } else {
                    0
                }
            })
            for (segment in lineSegments) {
                for (point in segment.straightLineLength) {
                    testMatrix[point.first, point.second] = 2
                }
            }
            lineSegments.filter { closestNeighbours.contains(it.id) }
                .forEach { segment ->
                    for (point in segment.straightLineLength) {
                        testMatrix[point.first, point.second] = 4
                    }
                }
            for (point in segment.straightLineLength) {
                testMatrix[point.first, point.second] = 3
            }
            return testMatrix

        }

        return Matrix(0, 0, { row, column -> 0 })
    }


    private fun examineSegments(lineSegment: LineSegment, closestNeighbours: List<LineSegment>) {

        // TODO
        println(lineSegment)


    }


    private fun findClosestNeighboursForSegment(
        segment: LineSegment,
        imageMatrix: Matrix<Boolean>,
        lineSegmentMatrix: Matrix<Int>
    ): MutableSet<Int> {
//        val updatableLineSegmentMatrix = Matrix.copy(lineSegmentMatrix)
        val processedPoints = Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns, { _, _ -> false })
        val closestNeighbours = mutableSetOf<Int>()
        val pointsToExamine = mutableListOf<Pair<Int, Int>>()
            .also { it.addAll(segment.straightLineLength) }

        while (pointsToExamine.isNotEmpty()) {
            val point = pointsToExamine.removeFirst()
            processedPoints[point.first, point.second] = true
            val neighbourhood = getNeighbourhood(imageMatrix, point)

            neighbourhood.forEachIndexed { innerRow, innerColumn, value ->
                if (value) {
                    val rowOffset = getOffset(innerRow)
                    val columnOffset = getOffset(innerColumn)

                    val neighbourRow = point.first + rowOffset
                    val neighbourColumn = point.second + columnOffset

                    if(!processedPoints[neighbourRow, neighbourColumn]) {
                        val neighbourValue = lineSegmentMatrix[neighbourRow, neighbourColumn]

                        if (lineSegmentMatrix[neighbourRow, neighbourColumn] != segment.id
                            && !closestNeighbours.contains(neighbourValue)
                        ) {
                            closestNeighbours.add(neighbourValue)

                            if (closestNeighbours.size == 3) {
                                return closestNeighbours
                            }
                        }

                        val currentPoint = Pair(neighbourRow, neighbourColumn)
                        if(!pointsToExamine.contains(currentPoint)) {
                            pointsToExamine.add(Pair(neighbourRow, neighbourColumn))
                        }
                    }
                }
            }

        }

        return closestNeighbours
    }


    companion object {

        fun createAreaExtracts(circlePaths: Iterable<CirclePath>): List<AreaExtract> {
            return circlePaths.map { AreaExtract(it.path.map { point -> point.circleCenter }) }
                .toList()
        }

    }

}