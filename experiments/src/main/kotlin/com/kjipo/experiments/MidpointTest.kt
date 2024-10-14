package com.kjipo.experiments

import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.getFlowDirectionArray
import com.kjipo.representation.raster.getNeighbourhood
import com.kjipo.representation.raster.getNeighbourhoodType
import representation.identifyRegions


class MidpointTest {


    fun extractBordersUntilNoneLeft(matrix: Matrix<Boolean>): List<List<Pair<Int, Int>>> {
        val copyMatrix = Matrix.copy(matrix)
        var border = extractBorders(copyMatrix)
        val borders = mutableListOf(border)

        do {
            border.forEach {
                copyMatrix[it.first, it.second] = false
            }

            border = extractBorders(copyMatrix)
            if (border.isNotEmpty()) {
                borders.add(border)
            }
        } while (border.isNotEmpty())

        return borders
    }


    fun findMidpointsInRegions(matrix: Matrix<Boolean>): Matrix<Boolean> {
        val regions = identifyRegions(matrix, 1)
        val borderLists = extractBordersUntilNoneLeft(matrix)

        val borderImage = Matrix(matrix.numberOfRows, matrix.numberOfColumns, { _, _ ->
            0
        })

        var distanceFromRegionEdge = 0
        for (borderList in borderLists) {
            borderList.forEach { borderImage[it.first, it.second] = distanceFromRegionEdge }
            ++distanceFromRegionEdge
        }

        var currentRegion = 1

        val numberOfRegions = regions.array.distinct().size - 1
        val midpointImage = Matrix(matrix.numberOfRows, matrix.numberOfColumns, { _, _ -> false })

        while (currentRegion <= numberOfRegions) {
            findMidpointsInRegion2(regions, currentRegion, borderImage, midpointImage)
            ++currentRegion
        }

        return midpointImage
    }


    private fun findMidpointsInRegion(
        regions: Matrix<Int>,
        currentRegion: Int,
        borderImage: Matrix<Int>,
        midpointImage: Matrix<Boolean>
    ) {
        val midPointsForRegion = mutableListOf<Pair<Int, Int>>()

        var maxValueForRegion = 0
        regions.forEachIndexed { row, column, value ->
            if (value == currentRegion) {
                if (borderImage[row, column] > maxValueForRegion) {
                    maxValueForRegion = borderImage[row, column]
                    midPointsForRegion.clear()
                    midPointsForRegion.add(Pair(row, column))
                } else if (borderImage[row, column] == maxValueForRegion) {
                    midPointsForRegion.add(Pair(row, column))
                }
            }
        }

        midPointsForRegion.forEach { midpointImage[it.first, it.second] = true }
        midPointsForRegion.clear()
    }

    private fun findMidpointsInRegion2(
        regions: Matrix<Int>,
        currentRegion: Int,
        borderImage: Matrix<Int>,
        midpointImage: Matrix<Boolean>
    ) {
        val midPointsForRegion = mutableListOf<Pair<Int, Int>>()

        var maxValueForRegion = 0
        regions.forEachIndexed { row, column, value ->
            if (value == currentRegion) {
                if (borderImage[row, column] > maxValueForRegion) {
                    maxValueForRegion = borderImage[row, column]
                    midPointsForRegion.clear()
                    midPointsForRegion.add(Pair(row, column))
                } else if (borderImage[row, column] == maxValueForRegion) {
                    midPointsForRegion.add(Pair(row, column))
                }
            }
        }

        if(midPointsForRegion.isNotEmpty()) {
            generatePath(midPointsForRegion.first(),
                regions,
                borderImage,
                midpointImage)
        }

    }


    private fun generatePath(startingPoint: Pair<Int, Int>,
                             regions: Matrix<Int>,
                             borderImage: Matrix<Int>,
                             midpointImage: Matrix<Boolean>): MutableList<Pair<Int, Int>> {

        val pointsToAddToPath = mutableListOf<Pair<Int, Int>>()

        pointsToAddToPath.add(startingPoint)

        val result = mutableListOf<Pair<Int, Int>>()

        while(pointsToAddToPath.isNotEmpty()) {
            val currentPoint = pointsToAddToPath.removeFirst()
            result.add(currentPoint)
            midpointImage[currentPoint.first, currentPoint.second] = true

            var maxDistanceInNeighbourhood = 0
            val newPointsToExamine = mutableListOf<Pair<Int, Int>>()
            val neighbourhood = getNeighbourhood(borderImage, currentPoint.first, currentPoint.second, getFlowDirectionArray())

            for (pair in neighbourhood) {
                val pointInNeighbourhood = Pair(currentPoint.first + pair.first.rowShift, currentPoint.second + pair.first.columnShift)

                if(pair.second
                    && regions[pointInNeighbourhood.first, pointInNeighbourhood.second] == regions[startingPoint.first, startingPoint.second]
                    && !midpointImage[pointInNeighbourhood.first, pointInNeighbourhood.second]) {
                    if(maxDistanceInNeighbourhood < borderImage[pointInNeighbourhood.first, pointInNeighbourhood.second]) {
                        newPointsToExamine.clear()
                        newPointsToExamine.add(pointInNeighbourhood)

//                        pointsToAddToPath.clear()
//                        pointsToAddToPath.add(currentPoint)
                        maxDistanceInNeighbourhood = borderImage[pointInNeighbourhood.first, pointInNeighbourhood.second]
                    }
                    else if(maxDistanceInNeighbourhood == borderImage[pointInNeighbourhood.first, pointInNeighbourhood.second]) {
                        newPointsToExamine.add(currentPoint)

//                        pointsToAddToPath.add(currentPoint)
                    }
                }
            }

            newPointsToExamine.filter { !pointsToAddToPath.contains(it) && !result.contains(it) }.forEach { pointsToAddToPath.add(it) }

        }

        return result

    }

}