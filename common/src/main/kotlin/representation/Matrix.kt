package com.kjipo.representation

import com.kjipo.representation.raster.EncodingUtilities
import com.kjipo.representation.raster.FlowDirection


class Matrix<T>(val numberOfRows: Int, val numberOfColumns: Int, val array: Array<Array<T>>) {

    companion object {

        inline operator fun <reified T> invoke() = Matrix(0, 0, Array(0, { emptyArray<T>() }))

        inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int) =
            Matrix(xWidth, yWidth, Array(xWidth, { arrayOfNulls<T>(yWidth) }))

        inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int, operator: (Int, Int) -> (T)): Matrix<T> {
            val array = Array(xWidth) { array ->
                Array(yWidth) { operator(array, it) }
            }
            return Matrix(xWidth, yWidth, array)
        }

        inline fun <reified T> copy(matrix: Matrix<T>): Matrix<T> {
            return Matrix(matrix.numberOfRows, matrix.numberOfColumns) { row, column ->
                matrix[row, column]
            }
        }

    }


    inline fun <reified T> getNeighbourhood(row: Int, column: Int): Matrix<T?> {
        val result = Matrix<T?>(3, 3) { _, _ -> null }
        result[1, 1] = array[row][column] as T?
        FlowDirection.entries.forEach {
            if (EncodingUtilities.validCell(row, column, it, numberOfRows, numberOfColumns)) {
                result[1 + it.rowShift, 1 + it.columnShift] = array[row + it.rowShift][column + it.columnShift] as T?
            }
        }
        return result
    }

    fun isValid(row: Int, column: Int): Boolean {
        return row >= 0 && row < array.size && column >= 0 && column < array[0].size
    }


    operator fun get(x: Int, y: Int): T {
        return array[x][y]
    }

    operator fun set(x: Int, y: Int, t: T) {
        array[x][y] = t
    }

    inline fun forEach(operation: (T) -> Unit) {
        array.forEach { it.forEach { operation.invoke(it) } }
    }

    inline fun forEachIndexed(operation: (x: Int, y: Int, T) -> Unit) {
        array.forEachIndexed { x, p -> p.forEachIndexed { y, t -> operation.invoke(x, y, t) } }
    }

    override fun toString(): String {
        val result = StringBuilder()
        array.forEach {
            it.forEach { result.append(it).append(" ") }
            result.append("\n")
        }
        return result.toString()
    }


}