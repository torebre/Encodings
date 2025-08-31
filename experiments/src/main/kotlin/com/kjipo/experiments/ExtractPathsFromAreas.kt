package com.kjipo.experiments

import com.kjipo.representation.LineUtilities.createLine
import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.getNeighbourhood
import com.kjipo.segmentation.getOffset
import representation.identifyRegions
import kotlin.Boolean
import kotlin.math.abs


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

    fun joinSegments(): JoinedSegmentData {
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

        return joinSegments2(pathSegments, imageMatrix, regionMatrix, pathMatrix)
    }


    fun joinSegments(
        lineSegments: List<LineSegment>,
        imageMatrix: Matrix<Boolean>,
        regionMatrix: Matrix<Int>,
        lineSegmentMatrix: Matrix<Int>
    ): Pair<List<LineSegment>, Matrix<Int>> {
        val segmentsByDescendingLength = lineSegments.sortedByDescending { it.length() }

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

        var counter = 0
        val resultingSegments = mutableListOf<LineSegment>()

        for (segment in segmentsByDescendingLength) {
            // TODO Only look at lines longer than 6 pixels to cut down on number of lines to examine while developing
//            if (segment.straightLineLength.size < 6) {
//                continue
//            }

            val closestNeighbours = findClosestNeighboursForSegment(segment, imageMatrix, lineSegmentMatrix)
            val segments = examineSegments(segment, lineSegments.filter { closestNeighbours.contains(it.id) })

            lineSegments.filter { closestNeighbours.contains(it.id) }
                .forEach { segment ->
                    for (point in segment.straightLineLength) {
                        testMatrix[point.first, point.second] = 4
                    }
                }
            for (point in segment.straightLineLength) {
                testMatrix[point.first, point.second] = 3
            }

            ++counter
            resultingSegments.addAll(segments)

            // TODO Only look at two segments while testing
            if (counter > 1) {
                break
            }

        }

        return Pair(lineSegments, testMatrix)
    }


    fun joinSegments2(
        lineSegments: List<LineSegment>,
        imageMatrix: Matrix<Boolean>,
        regionMatrix: Matrix<Int>,
        lineSegmentMatrix: Matrix<Int>
    ): JoinedSegmentData {
        val segmentsByDescendingLength = lineSegments.sortedByDescending { it.length() }

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

        var counter = 0
        val resultingSegments = mutableListOf<LineSegment>()

        val distanceMatrixStart = Matrix(lineSegments.size, lineSegments.size, { row, column ->
            -1
        })
        val distanceMatrixStop = Matrix(lineSegments.size, lineSegments.size, { row, column ->
            -1
        })

        val segmentIdIndexMap = mutableMapOf<Int, Int>()

        lineSegments.forEachIndexed { index, lineSegment ->
            val start = lineSegment.straightLineLength.first()
            val stop = lineSegment.straightLineLength.last()

            segmentIdIndexMap[lineSegment.id] = index

            lineSegments.forEachIndexed { index2, lineSegment2 ->
                if (index != index2
                    && distanceMatrixStart[index, index2] == -1
                    && distanceMatrixStop[index, index2] == -1
                ) {
                    distanceMatrixStart[index, index2] =
                        getManhattanDistance(start, lineSegment2.straightLineLength.first())
                    distanceMatrixStart[index2, index] = distanceMatrixStart[index, index2]
                    distanceMatrixStop[index, index2] =
                        getManhattanDistance(stop, lineSegment2.straightLineLength.last())
                    distanceMatrixStop[index2, index] = distanceMatrixStop[index, index2]
                }
            }
        }

        for (segment in segmentsByDescendingLength) {
            // TODO Only look at lines longer than 6 pixels to cut down on number of lines to examine while developing
//            if (segment.straightLineLength.size < 6) {
//                continue
//            }

//            val closestNeighbours = findClosestNeighboursForSegment(segment, imageMatrix, lineSegmentMatrix)

            val closestNeighbours = segmentIdIndexMap.keys.map { segmentId ->
                if (segment.id == segmentId) {
                    emptyList()
                } else {
                    listOf(
                        Pair(
                            segmentId,
                            distanceMatrixStart[segmentIdIndexMap[segment.id]!!, segmentIdIndexMap[segmentId]!!]
                        ),
                        Pair(
                            segmentId,
                            distanceMatrixStop[segmentIdIndexMap[segment.id]!!, segmentIdIndexMap[segmentId]!!]
                        )
                    )
                }
            }.flatten()
                .sortedBy { it.second }
                .take(3)
                .map { it.first }


//            for (segmentId in segmentIdIndexMap.keys) {
//                val distance = distanceMatrixStart[segmentIdIndexMap[segment.id]!!, segmentIdIndexMap[segmentId]!!]
//            }

            val segments = examineSegments(segment, lineSegments.filter { closestNeighbours.contains(it.id) })

            // Add colour to the closest neighbours
            lineSegments.filter { closestNeighbours.contains(it.id) }
                .forEach { segment ->
                    for (point in segment.straightLineLength) {
                        testMatrix[point.first, point.second] = 4
                    }
                }
            // Give a different colour to the segment that is being examined
            for (point in segment.straightLineLength) {
                testMatrix[point.first, point.second] = 3
            }

            ++counter
            resultingSegments.addAll(segments)

            // TODO Only look at one segments while testing
            break

        }

        return JoinedSegmentData(lineSegments, testMatrix)
    }


    private fun getManhattanDistance(point1: Pair<Int, Int>, point2: Pair<Int, Int>): Int {
        return abs(point1.first - point2.first) + abs(point1.second - point2.second)
    }


    private fun examineSegments(lineSegment: LineSegment, closestNeighbours: List<LineSegment>): List<LineSegment> {

        println("Line segment: ${lineSegment.lineSummary()}. Incline: ${lineSegment.incline()}")

        for (segment in closestNeighbours) {
            println("Line segment: ${segment.lineSummary()}. Incline: ${segment.incline()}")
        }

        // TODO
        println(lineSegment)

        // TODO Only like this for testing. Combine segments and return a new set of segments
        return listOf(lineSegment)
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

                    if (!processedPoints[neighbourRow, neighbourColumn]) {
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
                        if (!pointsToExamine.contains(currentPoint)) {
                            pointsToExamine.add(Pair(neighbourRow, neighbourColumn))
                        }
                    }
                }
            }

        }

        return closestNeighbours
    }


    fun compareSegmentData(joinedSegmentData: JoinedSegmentData, joinedSegmentData2: JoinedSegmentData) {
        val twoLongestSegments = joinedSegmentData.lineSegments.sortedBy { it.length() }.take(2)
        val twoLongestSegments2 = joinedSegmentData.lineSegments.sortedBy { it.length() }.take(2)

        // TODO


    }


    companion object {

        fun createAreaExtracts(circlePaths: Iterable<CirclePath>): List<AreaExtract> {
            return circlePaths.map { AreaExtract(it.path.map { point -> point.circleCenter }) }
                .toList()
        }

    }

}