package com.kjipo.skeleton

import com.kjipo.raster.EncodingUtilities
import com.kjipo.raster.FlowDirection
import com.kjipo.segmentation.Matrix
import kotlin.math.min


fun extractSkeleton(image: Matrix<Boolean>) {
    val thinImage = makeThin(image)
    extractJunctions(thinImage)

    // TODO


}

fun extractJunctions(thinImage: Matrix<Boolean>) {
    // TODO


}


fun bwmorphEndpoints(image: Matrix<Boolean>): Matrix<Boolean> {
    val result = Matrix(image.numberOfRows, image.numberOfColumns, { row, column -> false })

    for (row in 0 until image.numberOfRows) {
        for (column in 0 until image.numberOfColumns) {
            result[row, column] = isEnd(row, column, image)
        }
    }

    return result
}

fun isEnd(row: Int, column: Int, image: Matrix<Boolean>): Boolean {
    if (!image[row, column]) {
        return false
    }

    return pixelsFilled(row, column, image) == 2

}

fun pixelsFilled(row: Int, column: Int, image: Matrix<Boolean>): Int {
    var pixels = 0

    if (row > 0) {
        if (column > 0) {
            if (image[row - 1, column - 1]) {
                ++pixels
            }
        }
        if (column < image.numberOfColumns - 1) {
            if (image[row - 1, column + 1]) {
                ++pixels
            }
        }
        if (image[row - 1, column]) {
            ++pixels
        }
    }

    if (row < image.numberOfRows - 1) {
        if (column > 0) {
            if (image[row + 1, column - 1]) {
                ++pixels
            }
        }
        if (column < image.numberOfColumns - 1) {
            if (image[row + 1, column + 1]) {
                ++pixels
            }
        }
        if (image[row + 1, column]) {
            ++pixels
        }
    }

    if (column > 0) {
        if (image[row, column - 1]) {
            ++pixels
        }
    }
    if (column < image.numberOfColumns - 1) {
        if (image[row, column + 1]) {
            ++pixels
        }
    }

    if (image[row, column]) {
        ++pixels
    }

    return pixels
}


fun makeThin(image: Matrix<Boolean>) = thin(fillIsolatedHoles(image))


fun fillIsolatedHoles(image: Matrix<Boolean>): Matrix<Boolean> {
    val result = Matrix.copy(image)

    for (row in 0 until image.numberOfRows) {
        for (column in 0 until image.numberOfColumns) {
            if (result[row, column]) {
                continue
            }
            result[row, column] = pixelsFilled(row, column, image) == 8
        }
    }

    return result
}


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


/**
 * TODO This method does not work as it should
 */
fun thinNotWorking(image: Matrix<Boolean>): Matrix<Boolean> {
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


internal val lookup1 = booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, true, true, false, false, true, true,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, false, true, true, false, false, false, false, false, false, false, true,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, true, false, false, false, true,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, true, false, false, false, true,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, false, true, true, false, false, false, false, false, false, false, true,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, false, true, true, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, true, true, false, true, false, false, false, true,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, false, true, true, true, true, false, false, true, true, false, false)

internal val lookup2 = booleanArrayOf(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, true, true, true, false, true, false, true, true, false, false, false, false, true, false,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, true, true, false, false, false, false, false, true, true, false, false, true, false,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, true, true, true, true, false, false, false, true, true, false, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, false, true, true, true, false, true, false, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, true, false, true, false, false, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, true, true, false, false, true, true, false, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, true, true, false, true, false, false, true, false, true, true, false, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, true, true, false, true, false, true, true, true, false, true, false, true, false, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, true, false, true, false, true, false, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, true, false, true, false, false, true, false, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, true, false, true, false, false, true, false, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true)


fun thin(image: Matrix<Boolean>): Matrix<Boolean> {
    var previous = image
    var result: Matrix<Boolean>
    while (true) {
        result = Matrix(image.numberOfRows, image.numberOfColumns, { row, column -> false })
        applyLookup(applyLookup(previous, lookup1), lookup2).forEachIndexed({ row, column, value ->
            result[row, column] = previous[row, column] && value
        })

        var changed = false
        result.forEachIndexed({ row, column, value ->
            if (value != previous[row, column]) {
                changed = true
                return@forEachIndexed
            }
        })

        if (changed) {
            previous = result
        } else {
            return result
        }
    }
}


fun applyLookup(image: Matrix<Boolean>, lookup: BooleanArray): Matrix<Boolean> {
    if (image.numberOfRows != image.numberOfColumns) {
        throw IllegalArgumentException("Only square matrices supported")
    }
    return applyLookup3(image, lookup)
}

fun applyLookup3(image: Matrix<Boolean>, lookup: BooleanArray): Matrix<Boolean> {
    val result = Matrix.copy(image)
    image.forEachIndexed({ row, column, value ->
        run {
            // https://se.mathworks.com/help/images/ref/applylut.html
            var index = if (value) {
                16
            } else {
                0
            }
            FlowDirection.values().forEach { value2 ->
                index += if (EncodingUtilities.validCell(row, column, value2, image.numberOfRows, image.numberOfColumns)) {
                    if (!image[row + value2.rowShift, column + value2.columnShift]) {
                        0
                    } else {
                        when (value2) {
                            FlowDirection.EAST -> 2
                            FlowDirection.NORTH_EAST -> 4
                            FlowDirection.NORTH -> 32
                            FlowDirection.NORTH_WEST -> 256
                            FlowDirection.WEST -> 128
                            FlowDirection.SOUTH_WEST -> 64
                            FlowDirection.SOUTH -> 8
                            FlowDirection.SOUTH_EAST -> 1
                        }
                    }
                } else {
                    0
                }
            }
            result[row, column] = lookup[index]
        }

    })
    return result
}


