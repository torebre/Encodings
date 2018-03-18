package com.kjipo.segmentation

class Matrix<T>(val numberOfRows: Int, val numberOfColumns: Int, val array: Array<Array<T>>) {

    companion object {

        inline operator fun <reified T> invoke() = Matrix(0, 0, Array(0, { emptyArray<T>() }))

        inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int) =
                Matrix(xWidth, yWidth, Array(xWidth, { arrayOfNulls<T>(yWidth) }))

        inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int, operator: (Int, Int) -> (T)): Matrix<T> {
            val array = Array(xWidth, {
                val x = it
                Array(yWidth, { operator(x, it) })
            })
            return Matrix(xWidth, yWidth, array)
        }

        inline fun <reified T> copy(matrix: Matrix<T>): Matrix<T> {
            return Matrix(matrix.numberOfRows, matrix.numberOfColumns, { row, column ->
                matrix.get(row, column)
            })
        }


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


}