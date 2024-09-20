package com.kjipo.experiments

import com.kjipo.representation.Matrix
import representation.identifyRegions


class MidpointTest {


    fun findMidpoints(matrix: Matrix<Boolean>): List<List<Pair<Int, Int>>> {
        val copyMatrix = Matrix.copy(matrix)
        var border = extractBorders(copyMatrix)
        val borders = mutableListOf(border)

        do {
            border.forEach {
                copyMatrix[it.first, it.second] = false
                border = extractBorders(copyMatrix)

                if (border.isNotEmpty()) {
                    borders.add(border)
                }
            }
        } while (border.isNotEmpty())

        return borders
    }


    fun findMidpointsInRegions(matrix: Matrix<Boolean>): Matrix<Boolean> {
        val regions = identifyRegions(matrix, 1)
        val borderLists = findMidpoints(matrix)

        val borderImage = Matrix(matrix.numberOfRows, matrix.numberOfColumns, { _, _ ->
            0
        })

        var distanceFromRegionEdge = 0
        for (borderList in borderLists) {
            borderList.forEach { borderImage[it.first, it.second] = distanceFromRegionEdge }
            ++distanceFromRegionEdge
        }

        var currentRegion = 1
        val midPointsForRegion = mutableListOf<Pair<Int, Int>>()

        val numberOfRegions = regions.array.distinct().size - 1
        val midpointImage = Matrix(matrix.numberOfRows, matrix.numberOfColumns, { _, _ -> false })

        while (currentRegion <= numberOfRegions) {
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
            ++currentRegion
        }

        return midpointImage
    }


}