package com.kjipo.skeleton

import com.kjipo.prototype.FitPrototype
import com.kjipo.raster.EncodingUtilities
import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.bwmorphEndpoints
import com.kjipo.representation.raster.getNeighbourhood
import kotlin.math.abs
import kotlin.math.min
import javafx.scene.paint.Color


fun extractJunctions(thinImage: Matrix<Boolean>): Matrix<Boolean> {
//    %
//    % For a circular stroke, there are no "features" by this definition.
//    % Thus, for any partition of the pixels into regions, there should
//    % be at least one feature for the tracing algorithm to find.
//    %
//    % Input
//    %  T: [n x n boolean] thinned image.
//    %    images are binary, where true means "black"
//    %
//    % Output
//    %  SN: [n x n boolean] extracted features.
//    function SN = extract_junctions(T)

    val se = bwmorphEndpoints(thinImage) // bwmorph(T,'endpoints');
//    SB = T; % black pixels
    val sb = thinImage

//            sz = size(T,1);


//    lutS3 = makelut( @(P)fS3(P) , 3);
    val lutS3 = makelut(::fS3)

//    S3 = applylut(T,lutS3);

    val s3 = applylut(thinImage, lutS3)

//    % final criteria
//    SN = SE | (SB & S3);
    val sn = Matrix(thinImage.numberOfRows, thinImage.numberOfColumns, { row, column ->
        se[row, column] || (sb[row, column] && s3[row, column])
    })


//    % Check to see that each connected component has a feature.
//    % This is necessary to process circles in the image.
//    CC = bwconncomp(T, 8);
    val cc = bwconncomp(thinImage)

//    nCC = CC.NumObjects;
//    val nCC = cc.pixelIdsList.size

//    for c = 1:nCC
    // TODO Check that range is correct
    for (i in 0 until cc.pixelIdsList.size) {
//        pid = CC.PixelIdxList { c };
        val pid = cc.pixelIdsList[i]

        val snSum = pid.map { sn[it.first, it.second] }
            .map {
                if (it) {
                    1
                } else {
                    0
                }
            }
            .sum()

        if (snSum == 0) {
            pid.minBy { it.first }?.let {
                sn[it.first, it.second] = true
            }
        }


//    % We have a circle. Circles are generally drawn from the
//    % top, we choose the top pixel here
//        if sum(SN(pid)) == 0
//        [irow, icol] = ind2sub(sz, pid);
//        sel = argmin(irow);
//        SN(pid(sel)) = true;
//        end
//
//        end


    }


//    end

    return sn
}

data class ConnCompResult(val pixelIdsList: List<List<Pair<Int, Int>>>)

fun bwconncomp(image: Matrix<Boolean>): ConnCompResult {
    val disjointRegions = FitPrototype.findDisjointRegions(transformToBooleanArrays(image))

    val regionPixelMapping = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
    disjointRegions.forEachIndexed({ row, columnValues ->
        columnValues.forEachIndexed({ column, value ->
            regionPixelMapping.computeIfAbsent(value, { key -> mutableListOf() }).add(Pair(row, column))
        })
    })

    return ConnCompResult(regionPixelMapping.values.toList())
}


fun applylut(matrix: Matrix<Boolean>, lookupTable: List<Boolean>): Matrix<Boolean> {
    val result = Matrix.copy(matrix)

    matrix.forEachIndexed({ row, column, value ->
        val neighbourhood = getNeighbourhood(matrix, row, column)
        var lookupIndex = 0
        neighbourhood.forEachIndexed({ row2, column2, value2 ->
            if (value2) {
                lookupIndex += com.kjipo.skeleton.matrix[row2, column2]
            }
        })
        result[row, column] = lookupTable[lookupIndex]
    })

    return result
}




val matrix = Matrix(3, 3, { row, column ->
    when (Pair(row, column)) {
        Pair(0, 0) -> 256
        Pair(0, 1) -> 32
        Pair(0, 2) -> 4
        Pair(1, 0) -> 128
        Pair(1, 1) -> 16
        Pair(1, 2) -> 2
        Pair(2, 0) -> 64
        Pair(2, 1) -> 8
        Pair(2, 2) -> 1
        else -> throw IllegalArgumentException("Unexpected row: $row")
    }
})


fun makelut(function: (matrix: Matrix<Boolean>) -> Boolean): List<Boolean> {
//    nq=n^2;
//    c=2^nq;
//    lut=zeros(c,1);


//    256    32     4
//    128    16     2
//    64     8     1


    return (0 until 512).map {
        val evalMatrix = Matrix(3, 3, { row, column ->
            it.and(matrix[row, column]) > 0
        })

//        println("Matrix:\n${evalMatrix}")

        Pair(it, function.invoke(evalMatrix))
    }.sortedBy { it.first }
        .map { it.second }
        .toList()

//    w = reshape(2.^[nq - 1: - 1:0], n, n);
//    for i = 0:c-1
//    idx = bitand(w, i) > 0;
//    lut(i + 1) = feval(fun , idx, varargin { : });
//    endfor

}


//% See Liu et al.
//function Y=fS3(P)
fun fS3(matrix: Matrix<Boolean>): Boolean {
//sz = size(P);
//assert(isequal(sz,[3 3]));

//% Get cross number
    val nc = fNC(matrix);

//% Count black pixels
//PM = P;
    val pm = Matrix.copy(matrix)
    // TODO What does this do?
//PM(2,2) = false;
    pm[1, 1] = false
//NB = sum(PM(:));
    var nb = 0
    pm.forEach {
        nb += if (it) {
            1
        } else {
            0
        }
    }


//% Criteria
//Y = (NC >= 3-eps) || (NB >= 4-eps);
    return (nc >= 3 - Double.MIN_VALUE) || (nb >= 4 - Double.MIN_VALUE)

//end
}


//% See Liu et al.
//function Y=fNC(P)
fun fNC(matrix: Matrix<Boolean>): Double {
//    sum = 0;
    var sum = 0.0

//    for i = 0:7
    // TODO Check that the range is correct
    for (i in 0..7) {
//        sum = sum + abs(P(fIP(i + 1)) - P(fIP(i)));

        val (row1, column1) = fIP(i + 1)
        val (row2, column2) = fIP(i)

        sum += abs(
            if (matrix[row1, column1]) {
                1
            } else {
                0
            } - if (matrix[row2, column2]) {
                1
            } else {
                0
            }
        )
    }

//    end
    return sum / 2
//end
}

//% See Liu et al.
//function newlindx = fIP(lindx)
fun fIP(lindx: Int): Pair<Int, Int> {
    return when (lindx) {
        0, 8 -> Pair(0, 1)
        1 -> Pair(0, 2)
        2 -> Pair(1, 2)
        3 -> Pair(2, 2)
        4 -> Pair(2, 1)
        5 -> Pair(2, 0)
        6 -> Pair(1, 0)
        7 -> Pair(0, 0)
        else -> throw IllegalArgumentException("Unexpected index: $lindx")
    }
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
                    && mask[row - 1, column]
                ) {
                    if (!outputImage[row - 1, column]) {
                        change = true
                    }
                    outputImage[row - 1, column] = true
                }
                // Down
                if (EncodingUtilities.validCoordinates(row + 1, column, image.numberOfRows, image.numberOfColumns)
                    && mask[row + 1, column]
                ) {
                    if (!outputImage[row + 1, column]) {
                        change = true
                    }
                    outputImage[row + 1, column] = true
                }
                // Left
                if (EncodingUtilities.validCoordinates(row, column - 1, image.numberOfRows, image.numberOfColumns)
                    && mask[row, column - 1]
                ) {
                    if (!outputImage[row, column - 1]) {
                        change = true
                    }
                    outputImage[row, column - 1] = true
                }
                // Right
                if (EncodingUtilities.validCoordinates(row, column + 1, image.numberOfRows, image.numberOfColumns)
                    && mask[row, column + 1]
                ) {
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
                    || getNeighbour(row, column, 2 * i + 1, result))
        ) {
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
            || getNeighbour(row, column, 2 * k + 1, result)
        ) {
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
                                ) && getNeighbour(row, column, 1, result)
                    ) {
                        continue
                    }
                } else {
                    if (!(getNeighbour(row, column, 6, result)
                                || getNeighbour(row, column, 7, result)
                                || !getNeighbour(row, column, 4, result))
                        && getNeighbour(row, column, 5, result)
                    ) {
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


fun transformToBooleanArrays(image: Matrix<Boolean>): Array<BooleanArray> {
    return (0 until image.numberOfRows).map {
        val row = it
        (0 until image.numberOfColumns).map {
            image[row, it]
        }.toBooleanArray()

    }.toTypedArray()
}

fun transformToArrays(image: Matrix<Color>): Array<Array<Color>> {
    return (0 until image.numberOfRows).map {
        val row = it
        (0 until image.numberOfColumns).map {
            image[row, it]
        }.toTypedArray()

    }.toTypedArray()
}


fun transformArraysToMatrix(image: Array<BooleanArray>) =
    Matrix(image.size, image[0].size, { row, column -> image[row][column] })

fun transformArraysToMatrix(image: Array<IntArray>) =
    Matrix(image.size, image[0].size, { row, column -> image[row][column] })


fun makeSquare(matrix: Matrix<Boolean>): Matrix<Boolean> {
    if (matrix.numberOfRows == matrix.numberOfColumns) {
        return Matrix.copy(matrix)
    }

    var minRow = Int.MAX_VALUE
    var maxRow = 0
    var minColumn = Int.MAX_VALUE
    var maxColumn = 0

    matrix.forEachIndexed({ row, column, value ->
        if (matrix[row, column]) {
            if (minRow > row) {
                minRow = row
            }
            if (maxRow < row) {
                maxRow = row
            }
            if (minColumn > column) {
                minColumn = column
            }
            if (maxColumn < column) {
                maxColumn = column
            }
        }
    })

    val occupiedRows = maxRow - minRow
    val occupiedColumns = maxColumn - minColumn
    val sideSize = if (occupiedRows < occupiedColumns) {
        occupiedColumns
    } else {
        occupiedRows
    }

    return Matrix(sideSize, sideSize, { row, column ->
        val offsetRow = row + minRow
        val offsetColumn = column + minColumn

        if (offsetRow >= matrix.numberOfRows || offsetColumn >= matrix.numberOfColumns) {
            false
        } else {
            matrix[offsetRow, offsetColumn]
        }
    })


}