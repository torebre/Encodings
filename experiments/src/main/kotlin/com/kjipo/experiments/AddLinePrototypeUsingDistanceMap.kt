package com.kjipo.experiments

import com.kjipo.raster.EncodingUtilities
import com.kjipo.raster.FlowDirection
import com.kjipo.raster.TileType
import com.kjipo.raster.match.MatchDistance
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.computeLine
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.bwmorphEndpoints
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToBooleanArrays
import com.kjipo.visualization.displayColourMatrix
import com.kjipo.visualization.loadEncodedKanji
import javafx.scene.paint.Color
import java.nio.file.Paths


fun addLinePrototypeUsingDistanceMap() {
    val encodedKanji = loadEncodedKanji(Paths.get("kanji_output8/26681.dat"))
    val image = transformArraysToMatrix(encodedKanji.image)
    val shrinkImage = shrinkImage(image, 32, 32)
    val endPoints = bwmorphEndpoints(shrinkImage)
    val distanceMatrix = transformArraysToMatrix(MatchDistance.computeDistanceMap(transformToBooleanArrays(shrinkImage)))

    var maxValue = Int.MIN_VALUE
    distanceMatrix.forEach {
        if (it > maxValue) {
            maxValue = it
        }
    }

    var startCoordinates = Pair(0, 0)
    endPoints.forEachIndexed({row, column , value ->
        if(value) {
            startCoordinates = Pair(row, column)
            return@forEachIndexed
        }
    })

    val visited = addLinePrototype(startCoordinates, shrinkImage, distanceMatrix)

    val dispImage = Matrix(shrinkImage.numberOfRows, shrinkImage.numberOfColumns, { row, column ->
        val distance = distanceMatrix[row, column]
        Color.hsb(distance.toDouble().div(maxValue).times(360), 0.5, 0.2)
    })


    var counter = 0
    visited.forEach {
        dispImage[it.first.first, it.first.second] = Color.hsb(counter.toDouble().div(visited.size).times(360), 1.0, 1.0)
        ++counter
    }

    displayColourMatrix(dispImage, 20)
}



fun addLinePrototype(startCoordinate: Pair<Int, Int>, image: Matrix<Boolean>, distanceMatrix: Matrix<Int>): MutableList<Pair<Pair<Int, Int>, Int>> {
    val imageArray = transformToBooleanArrays(image)
    val visited = Matrix(image.numberOfRows, image.numberOfColumns, {row, column -> false})

    var currentCoordinate = startCoordinate
    visited[currentCoordinate.first, currentCoordinate.second] = true

    val scoreMatrix = Matrix(image.numberOfRows, image.numberOfColumns, {row, column ->
        0
    })
    scoreMatrix[currentCoordinate.first, currentCoordinate.second] = 0

    val path = mutableListOf<Pair<Pair<Int, Int>, Int>>()
    path.add(Pair(currentCoordinate, 0))

    while(true) {
        val neighbours = EncodingUtilities.determineNeighbourTypes(currentCoordinate.first, currentCoordinate.second, imageArray)
        val nextCell = neighbours.mapIndexed { index, tileType ->
            val flowDirection = FlowDirection.values()[index]
            Pair(Pair(currentCoordinate.first + flowDirection.rowShift, currentCoordinate.second + flowDirection.columnShift), when (tileType) {
//                TileType.OUTSIDE_CHARACTER,
                TileType.OPEN -> {
                    distanceMatrix[currentCoordinate.first + flowDirection.rowShift, currentCoordinate.second + flowDirection.columnShift]
                }
                else -> Int.MAX_VALUE
            })
        }
                .filter { it.second != Int.MAX_VALUE }
                .filter { !visited[it.first.first, it.first.second] }
                .map {
                    val lineSegment = computeLine(startCoordinate, currentCoordinate)
                    val score = lineSegment.map {
                        if (image[it.first, it.second]) {
                            1
                        } else {
                            0
                        }
                    }.sum()

                    Pair(it.first, score + it.second)
                }
                .sortedBy { it.second }
                .firstOrNull() ?: return path

        visited[nextCell.first.first, nextCell.first.second] = true
        scoreMatrix[nextCell.first.first, nextCell.first.second] = nextCell.second
        currentCoordinate = nextCell.first

        path.add(nextCell)
    }

    return path
}


fun main(args: Array<String>) {
    addLinePrototypeUsingDistanceMap()
}