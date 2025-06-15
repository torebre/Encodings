package com.kjipo.experiments

import com.kjipo.representation.LineUtilities.createLine
import com.kjipo.representation.Matrix
import representation.identifyRegions
import kotlin.Boolean


class ExtractPathsFromAreas(
    private val areaExtracts: List<AreaExtract>,
    private val imageMatrix: Matrix<Boolean>,
    private val regionMatrix: Matrix<Int>
) {

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


    companion object {

        fun createAreaExtracts(circlePaths: Iterable<CirclePath>): List<AreaExtract> {
            return circlePaths.map { AreaExtract(it.path.map { point -> point.circleCenter }) }
                .toList()
        }

    }

}