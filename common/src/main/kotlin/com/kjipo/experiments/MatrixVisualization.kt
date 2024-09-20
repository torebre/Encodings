package com.kjipo.experiments

import com.kjipo.representation.Matrix


data class PointColor(val red: Double, val green: Double, val blue: Double)


class MatrixVisualization<T>(val matrix: Matrix<T>, val colorFunction: (T) -> PointColor) {

    fun getColorForPoint(row: Int, column: Int): PointColor {
        return colorFunction(matrix[row, column])
    }

}