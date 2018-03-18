package com.kjipo.skeleton

import com.kjipo.raster.EncodingUtilities
import com.kjipo.segmentation.Matrix
import kotlin.math.min


fun extractSkeleton(image: Matrix<Boolean>) {

    makeThin(image)

    // TODO


}


fun makeThin(image: Matrix<Boolean>) = thin(binaryFillHoles(image))


fun binaryFillHoles(image: Matrix<Boolean>): Matrix<Boolean> {
    val outputImage = Matrix(image.numberOfRows, image.numberOfColumns, { row, column -> false })
    val mask = Matrix.copy(image)
    invertImage(mask)

    outputImage.forEachIndexed({ row, column, value ->
        if (row == 0 && mask[row, column]) {
            outputImage[row, column] = true
        } else if (row == image.numberOfRows - 1 && mask[row, column]) {
            outputImage[row, column] = true
        } else if (column == 0 && mask[row, column]) {
            outputImage[row, column] = true
        } else if (column == image.numberOfColumns - 1 && mask[row, column]) {
            outputImage[row, column] = true
        }
    })

    var change = false
    while (true) {
        outputImage.forEachIndexed({ row, column, value ->
            if (outputImage[row, column]) {
                // Up
                if (EncodingUtilities.validCoordinates(row - 1, column, image.numberOfRows, image.numberOfColumns)
                        && mask[row - 1, column]) {
                    if (!outputImage[row - 1, column]) {
                        change = true
                    }
                    outputImage[row - 1, column] = true
                }
                // Down
                if (EncodingUtilities.validCoordinates(row + 1, column, image.numberOfRows, image.numberOfColumns)
                        && mask[row + 1, column]) {
                    if (!outputImage[row + 1, column]) {
                        change = true
                    }
                    outputImage[row + 1, column] = true
                }
                // Left
                if (EncodingUtilities.validCoordinates(row, column - 1, image.numberOfRows, image.numberOfColumns)
                        && mask[row, column - 1]) {
                    if (!outputImage[row, column - 1]) {
                        change = true
                    }
                    outputImage[row, column - 1] = true
                }
                // Right
                if (EncodingUtilities.validCoordinates(row, column + 1, image.numberOfRows, image.numberOfColumns)
                        && mask[row, column + 1]) {
                    if (!outputImage[row, column + 1]) {
                        change = true
                    }
                    outputImage[row, column + 1] = true
                }
            }

        })

        if (!change) {
            break
        }
        change = false
    }

    return Matrix(image.numberOfRows, image.numberOfColumns, { row, column ->
        !outputImage[row, column]
    })
}

fun invertImage(image: Matrix<Boolean>) {
    image.forEachIndexed({ row, column, value -> image[row, column] = !value })
}


fun getNeighbour(row: Int, column: Int, neighbourParam: Int, image: Matrix<Boolean>): Boolean {
    var neighbour = neighbourParam

    if (neighbour > 8) {
        neighbour = neighbour % 9 + 1
    }

    if (neighbour == 1) {
        if (column == image.numberOfColumns - 1) {
            return false
        } else {
            return image[row, column + 1]
        }
    }
    if (neighbour == 2) {
        if (row == 0 || column == image.numberOfColumns - 1) {
            return false
        } else {
            return image[row - 1, column + 1]
        }
    }
    if (neighbour == 3) {
        if (row == 0) {
            return false
        } else {
            return image[row - 1, column]
        }
    }
    if (neighbour == 4) {
        if (row == 0 || column == 0) {
            return false
        } else {
            return image[row - 1, column - 1]
        }
    }
    if (neighbour == 5) {
        if (column == 0) {
            return false
        } else {
            return image[row, column - 1]
        }
    }
    if (neighbour == 6) {
        if (row == image.numberOfRows - 1 || column == 0) {
            return false
        } else {
            return image[row + 1, column - 1]
        }
    }
    if (neighbour == 7) {
        if (row == image.numberOfRows - 1) {
            return false
        } else {
            return image[row + 1, column]
        }
    }
    if (neighbour == 8) {
        if (row == image.numberOfRows - 1 || column == image.numberOfColumns - 1) {
            return false
        } else {
            return image[row + 1, column + 1]
        }
    }

    throw IllegalStateException("Unexpected neighbour number")
}

fun getXh(row: Int, column: Int, result: Matrix<Boolean>): Int {
    var sum = 0
    for (i in 1..4) {
        if (!getNeighbour(row, column, 2 * i - 1, result)
                && (getNeighbour(row, column, 2 * i, result)
                        || getNeighbour(row, column, 2 * i + 1, result))) {
            sum += 1
        }
    }
    return sum
}

fun getN1(row: Int, column: Int, result: Matrix<Boolean>): Int {
    var sum = 0
    for (k in 1..4) {
        if (getNeighbour(row, column, 2 * k - 1, result)) {
            sum += 1
        }
    }
    return sum
}

fun getN2(row: Int, column: Int, result: Matrix<Boolean>): Int {
    var sum = 0
    for (k in 1..4) {
        if (getNeighbour(row, column, 2 * k, result)
                || getNeighbour(row, column, 2 * k + 1, result)) {
            sum += 1
        }
    }
    return sum
}

fun thin(image: Matrix<Boolean>): Matrix<Boolean> {
    val result = Matrix.copy(image)
    var first = true
    var deletion = true

    val toDelete = mutableListOf<Pair<Int, Int>>()

    while (deletion) {
        deletion = false

        for (row in 0 until image.numberOfRows) {
            for (column in 0 until image.numberOfColumns) {
                if (!result[row, column]) {
                    continue
                }

                val xH = getXh(row, column, result)

                if (xH != 1) {
                    continue
                }

                val n1 = getN1(row, column, result)
                val n2 = getN2(row, column, result)

                val nMin = min(n1, n2)

                if (nMin < 2 || nMin > 3) {
                    continue
                }

                if (first) {
                    first = false
                    if (!(getNeighbour(row, column, 2, result)
                                    || getNeighbour(row, column, 3, result)
                                    || !getNeighbour(row, column, 8, result)
                                    ) && getNeighbour(row, column, 1, result)) {
                        continue
                    }
                } else {
                    if (!(getNeighbour(row, column, 6, result)
                                    || getNeighbour(row, column, 7, result)
                                    || !getNeighbour(row, column, 4, result))
                            && getNeighbour(row, column, 5, result)) {
                        continue
                    }
                }


                toDelete.add(Pair(row, column))
                deletion = true


            }
        }



        toDelete.forEach {
            result[it.first, it.second] = false
        }
        toDelete.clear()
    }


    return result


}


