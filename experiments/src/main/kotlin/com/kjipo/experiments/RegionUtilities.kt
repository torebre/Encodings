package com.kjipo.experiments

import com.kjipo.representation.Matrix
import representation.interiorPointRegion



inline fun <reified T> removeInteriorPoints(startPoint: Pair<Int, Int>, valueMatrix: Matrix<T>, stopValue: T) {
//    val pointsInInterior = mutableListOf<Pair<Int, Int>>()

    val pointsToExamine = mutableListOf<Pair<Int, Int>>()
    pointsToExamine.add(startPoint)

    var pointsRemoved = 0

    while(pointsToExamine.isNotEmpty()) {
        val point = pointsToExamine.removeFirst()

        valueMatrix[point.first, point.second] = stopValue
        ++pointsRemoved

        val neighbourhood = valueMatrix.getNeighbourhood<T?>(point.first, point.second)

        neighbourhood.forEachIndexed { row, column, value ->
            if(value != null && valueMatrix[point.first + row, point.second + column] != stopValue) {
                valueMatrix[row, column] = stopValue
                pointsToExamine.add(Pair(row, column))
            }
        }
    }

    println("Points removed: $pointsRemoved")

}
