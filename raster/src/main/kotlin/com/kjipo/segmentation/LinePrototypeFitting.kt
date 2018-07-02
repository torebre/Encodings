package com.kjipo.segmentation

import com.kjipo.prototype.AngleLine
import com.kjipo.prototype.FitPrototype
import com.kjipo.raster.EncodingUtilities.validCoordinates
import com.kjipo.raster.FlowDirection
import com.kjipo.raster.match.MatchDistance
import com.kjipo.raster.segment.Pair
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToBooleanArrays
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.max
import kotlin.streams.toList


val linePrototypeFittingLog = LoggerFactory.getLogger("LinePrototypeFitting")

fun fitLinePrototypes(originalImage: Matrix<Boolean>): List<AngleLine> {
    val shrinkImage = Matrix.copy(originalImage)
    val allPrototypes = mutableListOf<AngleLine>()

    var totalFilledPixels = 0
    shrinkImage.forEachIndexed({ _, _, value ->
        if (value) {
            ++totalFilledPixels
        }
    })

    val distanceMatrix = transformArraysToMatrix(MatchDistance.computeDistanceMap(transformToBooleanArrays(shrinkImage)))
    var maxValue = Int.MIN_VALUE
    distanceMatrix.forEach {
        if (it > maxValue) {
            maxValue = it
        }
    }


    while (true) {
        var startPair = Pair(0, 0)
        shrinkImage.forEachIndexed({ row, column, value ->
            if (value) {
                startPair = Pair(row, column)
                return@forEachIndexed
            }
        })

        linePrototypeFittingLog.info("Start pair: $startPair")

        val topPair = Pair.of(startPair.row, startPair.column)
        val topId = 1
        val top = AngleLine(topId, topPair, 0.0, 0.0)

        val prototype = fitSingleLine(shrinkImage, top)
        allPrototypes.add(prototype)

        var filledPixels = 0
        val tempImage = Matrix.copy(originalImage)

        prototype.segments.flatMap { it.pairs }.let {
            var first = true

            for (pair in it) {
                if (first) {
                    first = false
                    shrinkImage[pair.row, pair.column] = false

                    continue
                }

                if (pair.row < shrinkImage.numberOfRows && pair.row >= 0 && pair.column < shrinkImage.numberOfColumns && pair.column >= 0) {
                    val centerRow = pair.row
                    val centerColumn = pair.column

                    fun fillFunction(row: Int, column: Int): Boolean {
                        val rowOffset = row - 1
                        val columnOffset = column - 1

                        val currentRow = centerRow - rowOffset
                        val currentColumn = centerColumn - columnOffset

                        return if (currentRow < 0 || currentRow >= shrinkImage.numberOfRows || currentColumn < 0 || currentColumn >= shrinkImage.numberOfColumns) {
                            false
                        } else {
                            shrinkImage[currentRow, currentColumn]
                        }
                    }

                    val tempMatrix = Matrix(3, 3, ::fillFunction)
                    val originalValue = shrinkImage[pair.row, pair.column]

                    if ((tempMatrix[1, 0] && tempMatrix[1, 1] && tempMatrix[1, 2])
                            || (tempMatrix[0, 1] && tempMatrix[1, 1] && tempMatrix[1, 2])
                            || (tempMatrix[0, 0] && tempMatrix[1, 1] && tempMatrix[2, 2])
                            || (tempMatrix[2, 2] && tempMatrix[1, 1] && tempMatrix[0, 2])) {
                        shrinkImage[pair.row, pair.column] = originalValue
                    } else {
                        shrinkImage[pair.row, pair.column] = false
                    }
                }
            }

        }

        allPrototypes.stream()
                .flatMap { it.segments.stream() }
                .flatMap { it.pairs.stream() }
                .forEach {
                    filledPixels += if (it.row < shrinkImage.numberOfRows && it.row >= 0 && it.column < shrinkImage.numberOfColumns && it.column >= 0 && tempImage[it.row, it.column]) {
                        tempImage[it.row, it.column] = false
                        1
                    } else {
                        0
                    }
                }

        if (filledPixels == totalFilledPixels) {
            break
        }

    }

    return allPrototypes
}


fun fitLinePrototypes2(originalImage: Matrix<Boolean>): List<AngleLine> {
    val shrinkImage = Matrix.copy(originalImage)
    val allPrototypes = mutableListOf<AngleLine>()

    var totalFilledPixels = 0
    shrinkImage.forEachIndexed({ _, _, value ->
        if (value) {
            ++totalFilledPixels
        }
    })

    val distanceMatrix = transformArraysToMatrix(MatchDistance.computeDistanceMap(transformToBooleanArrays(shrinkImage)))
    var maxValue = Int.MIN_VALUE
    distanceMatrix.forEach {
        if (it > maxValue) {
            maxValue = it
        }
    }

    while (true) {
        var startPair = Pair(0, 0)
        shrinkImage.forEachIndexed({ row, column, value ->
            if (value) {
                startPair = Pair(row, column)
                return@forEachIndexed
            }
        })


        linePrototypeFittingLog.info("Start pair: $startPair")


        val topPair = Pair.of(startPair.row, startPair.column)
        val topId = 1
        val top = AngleLine(topId, topPair, 0.0, 0.0)

        val allLines = listOf(top)
        val fitPrototype = FitPrototype()

        val imageAsArrays = transformToBooleanArrays(shrinkImage)
        val prototypes = fitPrototype.addPrototypes(imageAsArrays, allLines, false).stream()
                .map { listOf(it) }
                .toList()


        // There is only one prototype in this case
        val prototype = prototypes[0][0] as AngleLine
        allPrototypes.add(prototype)

        var filledPixels = 0
        val tempImage = Matrix.copy(originalImage)

        prototype.segments.flatMap { it.pairs }.let {
            var first = true

            for (pair in it) {
                if (first) {
                    first = false
                    shrinkImage[pair.row, pair.column] = false

                    continue
                }

                if (pair.row < shrinkImage.numberOfRows && pair.row >= 0 && pair.column < shrinkImage.numberOfColumns && pair.column >= 0) {
                    val centerRow = pair.row
                    val centerColumn = pair.column

                    fun fillFunction(row: Int, column: Int): Boolean {
                        val rowOffset = row - 1
                        val columnOffset = column - 1

                        val currentRow = centerRow - rowOffset
                        val currentColumn = centerColumn - columnOffset

                        return if (currentRow < 0 || currentRow >= shrinkImage.numberOfRows || currentColumn < 0 || currentColumn >= shrinkImage.numberOfColumns) {
                            false
                        } else {
                            shrinkImage[currentRow, currentColumn]
                        }
                    }

                    val tempMatrix = Matrix(3, 3, ::fillFunction)
                    val originalValue = shrinkImage[pair.row, pair.column]

                    if ((tempMatrix[1, 0] && tempMatrix[1, 1] && tempMatrix[1, 2])
                            || (tempMatrix[0, 1] && tempMatrix[1, 1] && tempMatrix[1, 2])
                            || (tempMatrix[0, 0] && tempMatrix[1, 1] && tempMatrix[2, 2])
                            || (tempMatrix[2, 2] && tempMatrix[1, 1] && tempMatrix[0, 2])) {
                        shrinkImage[pair.row, pair.column] = originalValue
                    } else {
                        shrinkImage[pair.row, pair.column] = false
                    }
                }
            }

        }

        allPrototypes.stream()
                .flatMap { it.segments.stream() }
                .flatMap { it.pairs.stream() }
                .forEach {
                    filledPixels += if (it.row < shrinkImage.numberOfRows && it.row >= 0 && it.column < shrinkImage.numberOfColumns && it.column >= 0 && tempImage[it.row, it.column]) {
                        tempImage[it.row, it.column] = false
                        1
                    } else {
                        0
                    }
                }

        if (filledPixels == totalFilledPixels) {
            break
        }

    }

    return allPrototypes
}


private fun fitSingleLine(inputData: Matrix<Boolean>, startPrototype: AngleLine): AngleLine {
    val distanceMap = MatchDistance.computeDistanceMap(transformToBooleanArrays(inputData))
    val distanceMatrix = transformArraysToMatrix(distanceMap)

    val tabooSet = mutableSetOf<kotlin.Pair<Pair, Pair>>()

    var bestScore = Int.MIN_VALUE
    var bestPrototype = AngleLine(1, Pair(0, 0), 1.0, 0.0)

    for (movement in startPrototype.movements) {
        val startAndEnd = kotlin.Pair(movement.startPair, movement.endPair)
        if (tabooSet.contains(startAndEnd)) {
            continue
        }
        tabooSet.add(startAndEnd)

        var invalidCoordinate = false
        for (pair in movement.segments[0].pairs) {
            if (!FitPrototype.validCoordinates(pair, inputData.numberOfRows, inputData.numberOfColumns)) {
                invalidCoordinate = true
                break
            }
        }
        if (invalidCoordinate) {
            continue
        }

        var distanceScore = 0

        val scoreHistory = mutableListOf<Int>()
        val movementHistory = mutableListOf<AngleLine>()

        // There is only one segment in a line
        for (pair in movement.segments[0].pairs) {
            distanceScore -= distanceMatrix[pair.row, pair.column]
        }

        scoreHistory.add(distanceScore)
        movementHistory.add(movement)

        val results = expandFit(movementHistory, distanceMatrix, tabooSet, scoreHistory)

        if (bestScore < results.second) {
            bestScore = results.second
            bestPrototype = results.first
        }
    }

    return bestPrototype
}


private fun expandFit(prototypeHistory: MutableList<AngleLine>, distanceMatrix: Matrix<Int>, tabooSet: MutableSet<kotlin.Pair<Pair, Pair>>, scoreHistory: MutableList<Int>): kotlin.Pair<AngleLine, Int> {
    if (tabooSet.contains(kotlin.Pair(prototypeHistory.last().startPair, prototypeHistory.last().endPair))) {
        return kotlin.Pair(prototypeHistory.last(), scoreHistory.last())
    }
    tabooSet.add(kotlin.Pair(prototypeHistory.last().startPair, prototypeHistory.last().endPair))

    if (scoreHistory.size > 3) {

        println("Score history: $scoreHistory")

        if (scoreHistory[scoreHistory.lastIndex] <= scoreHistory[scoreHistory.lastIndex - 1] && scoreHistory[scoreHistory.lastIndex - 1] <= scoreHistory[scoreHistory.lastIndex - 2]) {
            var bestScore = Int.MIN_VALUE

            var previousValue = scoreHistory.last()
            var index = prototypeHistory.lastIndex
            for (score in scoreHistory.asReversed()) {
                if (score > previousValue) {
                    if (bestScore < score) {
                        return kotlin.Pair(prototypeHistory[index], score)
                    }
                }
                previousValue = score
                --index
            }
        }
    }

    println("Prototype: ${prototypeHistory.last()}")

    var bestScore = Int.MIN_VALUE
    var bestPrototype = AngleLine(1, Pair(0, 0), 1.0, 0.0)

    for (movement in prototypeHistory.last().movements) {
        val startAndEnd = kotlin.Pair(movement.startPair, movement.endPair)
        if (tabooSet.contains(startAndEnd)) {
            continue
        }
        tabooSet.add(startAndEnd)

        var invalidCoordinate = false
        for (pair in movement.segments[0].pairs) {
            if (!FitPrototype.validCoordinates(pair, distanceMatrix.numberOfRows, distanceMatrix.numberOfColumns)) {
                invalidCoordinate = true
                break
            }
        }
        if (invalidCoordinate) {
            continue
        }

        if (distanceMatrix[movement.startPair.row, movement.startPair.column] != 0
                || distanceMatrix[movement.endPair.row, movement.endPair.column] != 0) {
            continue
        }

        var distanceScore = 0
        // There is only one segment in a line
        for (pair in movement.segments[0].pairs) {
            distanceScore -= distanceMatrix[pair.row, pair.column]
        }


        val updatedScoreHistory = mutableListOf<Int>()
        updatedScoreHistory.addAll(scoreHistory)
        val updatedPrototypeHistory = mutableListOf<AngleLine>()
        updatedPrototypeHistory.addAll(prototypeHistory)

        updatedScoreHistory.add(distanceScore)
        updatedPrototypeHistory.add(movement)

        val updatedFit = expandFit(updatedPrototypeHistory, distanceMatrix, tabooSet, updatedScoreHistory)


        if (bestScore < updatedFit.second) {
            bestScore = updatedFit.second
            bestPrototype = updatedFit.first
        }
    }

    return kotlin.Pair(bestPrototype, bestScore)
}

fun fitSingleLine2(inputData: Matrix<Boolean>, startPrototype: AngleLine): AngleLine {
    val distanceMap = MatchDistance.computeDistanceMap(transformToBooleanArrays(inputData))
    val distanceMatrix = transformArraysToMatrix(distanceMap)

    val tabooSet = mutableSetOf<kotlin.Pair<Pair, Pair>>()

    var bestScore = Int.MIN_VALUE
    var bestPrototype = AngleLine(1, Pair(0, 0), 1.0, 0.0)
    var maxLength = bestPrototype.length

    val processQueue = ArrayDeque<AngleLine>()
    processQueue.add(startPrototype)


    while (!processQueue.isEmpty()) {
        val currentElement = processQueue.element()

        for (movement in currentElement.movements) {
            val startAndEnd = kotlin.Pair(movement.startPair, movement.endPair)
            if (tabooSet.contains(startAndEnd)) {
                continue
            }
            tabooSet.add(startAndEnd)

            var invalidCoordinate = false
            for (pair in movement.segments[0].pairs) {
                if (!FitPrototype.validCoordinates(pair, inputData.numberOfRows, inputData.numberOfColumns)) {
                    invalidCoordinate = true
                    break
                }
            }
            if (invalidCoordinate) {
                continue
            }


            if (movement.length < maxLength) {
                continue
            }

            val results = expandFit2(currentElement, distanceMatrix, tabooSet)

            if (bestScore < results.second && results.first.length <= maxLength) {
                bestScore = results.second
                bestPrototype = results.first
                maxLength = bestPrototype.length

                println("Best prototype: $bestPrototype. Score: $bestScore. Length: ${maxLength}")
            }
        }

    }

    return bestPrototype
}


private fun expandFit2(prototype: AngleLine, distanceMatrix: Matrix<Int>, tabooSet: MutableSet<kotlin.Pair<Pair, Pair>>): kotlin.Pair<AngleLine, Int> {
    var bestScore = Int.MIN_VALUE
    var bestPrototype = AngleLine(1, Pair(0, 0), 1.0, 0.0)

    for (movement in prototype.movements) {
        val startAndEnd = kotlin.Pair(movement.startPair, movement.endPair)
        if (tabooSet.contains(startAndEnd)) {
            continue
        }
        tabooSet.add(startAndEnd)

        var invalidCoordinate = false
        for (pair in movement.segments[0].pairs) {
            if (!FitPrototype.validCoordinates(pair, distanceMatrix.numberOfRows, distanceMatrix.numberOfColumns)) {
                invalidCoordinate = true
                break
            }
        }
        if (invalidCoordinate) {
            continue
        }

//        if(distanceMatrix[movement.startPair.row, movement.startPair.column] != 0
//                || distanceMatrix[movement.endPair.row, movement.endPair.column] != 0) {
//            continue
//        }

        var distanceScore = 0
        // There is only one segment in a line
        for (pair in movement.segments[0].pairs) {
            distanceScore -= distanceMatrix[pair.row, pair.column]
        }

        if (bestScore < distanceScore) {
            bestScore = distanceScore
            bestPrototype = movement
        }


    }

    return kotlin.Pair(bestPrototype, bestScore)
}


fun fitSingleLine3(inputData: Matrix<Boolean>, startPair: kotlin.Pair<Int, Int>): AngleLine {
    val distanceMap = MatchDistance.computeDistanceMap(transformToBooleanArrays(inputData))
    val distanceMatrix = transformArraysToMatrix(distanceMap)
    val tabooSet = mutableSetOf<kotlin.Pair<Int, Int>>()

    var bestScore = Int.MIN_VALUE
    var bestPrototype = AngleLine(1, Pair(0, 0), 1.0, 0.0)
    val processQueue = ArrayDeque<kotlin.Pair<Int, Int>>()
    processQueue.add(startPair)
    tabooSet.add(startPair)


    while (!processQueue.isEmpty()) {
        val currentElement = processQueue.poll()

        val line = AngleLine(1, Pair(startPair.first, startPair.second), Pair(currentElement.first, currentElement.second))
        var distanceScore = 0
        for (pair in line.segments[0].pairs) {
            distanceScore -= distanceMatrix[pair.row, pair.column]
        }

        if (bestScore <= distanceScore) {
            bestScore = distanceScore
            bestPrototype = line

            println("Best prototype: $bestPrototype. Score: $bestScore")
        }



        for (value in FlowDirection.values()) {
            val shiftedElement = kotlin.Pair(currentElement.first + value.rowShift, currentElement.second + value.columnShift)

            if (!FitPrototype.validCoordinates(shiftedElement.first, shiftedElement.second, inputData.numberOfRows, inputData.numberOfColumns)) {
                continue
            }

            if (!inputData[shiftedElement.first, shiftedElement.second]
                    || tabooSet.contains(shiftedElement)) {

                println("Test24: $shiftedElement")

                continue
            }

            println("Test23: " +shiftedElement)

            tabooSet.add(shiftedElement)
            processQueue.add(shiftedElement)
        }

    }

    return bestPrototype
}
