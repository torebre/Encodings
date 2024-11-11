package com.kjipo.representation.raster

import com.kjipo.representation.Matrix

//fun extractJunctions(thinImage: Matrix<Boolean>): Matrix<Boolean> {
////    %
////    % For a circular stroke, there are no "features" by this definition.
////    % Thus, for any partition of the pixels into regions, there should
////    % be at least one feature for the tracing algorithm to find.
////    %
////    % Input
////    %  T: [n x n boolean] thinned image.
////    %    images are binary, where true means "black"
////    %
////    % Output
////    %  SN: [n x n boolean] extracted features.
////    function SN = extract_junctions(T)
//
//    val se = bwmorphEndpoints(thinImage) // bwmorph(T,'endpoints');
////    SB = T; % black pixels
//    val sb = thinImage
//
////            sz = size(T,1);
//
//
////    lutS3 = makelut( @(P)fS3(P) , 3);
//    val lutS3 = makelut(::fS3)
//
////    S3 = applylut(T,lutS3);
//
//    val s3 = applylut(thinImage, lutS3)
//
////    % final criteria
////    SN = SE | (SB & S3);
//    val sn = Matrix(thinImage.numberOfRows, thinImage.numberOfColumns, { row, column ->
//        se[row, column] || (sb[row, column] && s3[row, column])
//    })
//
//
////    % Check to see that each connected component has a feature.
////    % This is necessary to process circles in the image.
////    CC = bwconncomp(T, 8);
//    val cc = bwconncomp(thinImage)
//
////    nCC = CC.NumObjects;
////    val nCC = cc.pixelIdsList.size
//
////    for c = 1:nCC
//    // TODO Check that range is correct
//    for (i in 0 until cc.pixelIdsList.size) {
////        pid = CC.PixelIdxList { c };
//        val pid = cc.pixelIdsList[i]
//
//        val snSum = pid.map { sn[it.first, it.second] }
//                .map {
//                    if (it) {
//                        1
//                    } else {
//                        0
//                    }
//                }
//                .sum()
//
//        if (snSum == 0) {
//            pid.minBy { it.first }?.let {
//                sn[it.first, it.second] = true
//            }
//        }
//
//
////    % We have a circle. Circles are generally drawn from the
////    % top, we choose the top pixel here
////        if sum(SN(pid)) == 0
////        [irow, icol] = ind2sub(sz, pid);
////        sel = argmin(irow);
////        SN(pid(sel)) = true;
////        end
////
////        end
//
//
//    }
//
//
////    end
//
//    return sn
//}
//
//fun applylut(matrix: Matrix<Boolean>, lookupTable: List<Boolean>): Matrix<Boolean> {
//    val result = Matrix.copy(matrix)
//
//    matrix.forEachIndexed({ row, column, value ->
//        val neighbourhood = getNeighbourhood(matrix, row, column)
//        var lookupIndex = 0
//        neighbourhood.forEachIndexed({ row2, column2, value2 ->
//            if (value2) {
//                lookupIndex += matrix[row2, column2]
//            }
//        })
//        result[row, column] = lookupTable[lookupIndex]
//    })
//
//    return result
//}
//
//fun makelut(function: (matrix: Matrix<Boolean>) -> Boolean): List<Boolean> {
////    nq=n^2;
////    c=2^nq;
////    lut=zeros(c,1);
//
//
////    256    32     4
////    128    16     2
////    64     8     1
//
//
//    return (0 until 512).map {
//        val evalMatrix = Matrix(3, 3, { row, column ->
//            it.and(matrix[row, column]) > 0
//        })
//
////        println("Matrix:\n${evalMatrix}")
//
//        Pair(it, function.invoke(evalMatrix))
//    }.sortedBy { it.first }
//            .map { it.second }
//            .toList()
//
////    w = reshape(2.^[nq - 1: - 1:0], n, n);
////    for i = 0:c-1
////    idx = bitand(w, i) > 0;
////    lut(i + 1) = feval(fun , idx, varargin { : });
////    endfor
//
//}
//
////% See Liu et al.
////function Y=fS3(P)
//fun fS3(matrix: Matrix<Boolean>): Boolean {
////sz = size(P);
////assert(isequal(sz,[3 3]));
//
////% Get cross number
//    val nc = _root_ide_package_.com.kjipo.representation.raster.fNC(matrix);
//
////% Count black pixels
////PM = P;
//    val pm = Matrix.copy(matrix)
//    // TODO What does this do?
////PM(2,2) = false;
//    pm[1, 1] = false
////NB = sum(PM(:));
//    var nb = 0
//    pm.forEach {
//        nb += if (it) {
//            1
//        } else {
//            0
//        }
//    }
//
//
////% Criteria
////Y = (NC >= 3-eps) || (NB >= 4-eps);
//    return (nc >= 3 - Double.MIN_VALUE) || (nb >= 4 - Double.MIN_VALUE)
//
////end
//}
//
////% See Liu et al.
////function Y=fNC(P)
//fun fNC(matrix: Matrix<Boolean>): Double {
////    sum = 0;
//    var sum = 0.0
//
////    for i = 0:7
//    // TODO Check that the range is correct
//    for (i in 0..7) {
////        sum = sum + abs(P(fIP(i + 1)) - P(fIP(i)));
//
//        val (row1, column1) = _root_ide_package_.com.kjipo.representation.raster.fIP(i + 1)
//        val (row2, column2) = _root_ide_package_.com.kjipo.representation.raster.fIP(i)
//
//        sum += _root_ide_package_.kotlin.math.abs(if (matrix[row1, column1]) {
//            1
//        } else {
//            0
//        } - if (matrix[row2, column2]) {
//            1
//        } else {
//            0
//        })
//    }
//
////    end
//    return sum / 2
////end
//}
//
////% See Liu et al.
////function newlindx = fIP(lindx)
//fun fIP(lindx: Int): Pair<Int, Int> {
//    return when (lindx) {
//        0, 8 -> Pair(0, 1)
//        1 -> Pair(0, 2)
//        2 -> Pair(1, 2)
//        3 -> Pair(2, 2)
//        4 -> Pair(2, 1)
//        5 -> Pair(2, 0)
//        6 -> Pair(1, 0)
//        7 -> Pair(0, 0)
//        else -> throw IllegalArgumentException("Unexpected index: $lindx")
//    }
//}
//
//fun bwmorphEndpoints(image: Matrix<Boolean>): Matrix<Boolean> {
//    // TODO Does not mark everything that looks like an endpoint. Try to find a better algorithm
//
//    val result = Matrix(image.numberOfRows, image.numberOfColumns, { row, column -> false })
//
//    for (row in 0 until image.numberOfRows) {
//        for (column in 0 until image.numberOfColumns) {
//            result[row, column] = _root_ide_package_.com.kjipo.representation.raster.isEnd(row, column, image)
//        }
//    }
//
//    return result
//}
//
//fun isEnd(row: Int, column: Int, image: Matrix<Boolean>): Boolean {
//    return if (!image[row, column]) {
//        false
//    } else {
//        _root_ide_package_.com.kjipo.representation.raster.pixelsFilled(row, column, image) == 2
//    }
//}
//
//fun pixelsFilled(row: Int, column: Int, image: Matrix<Boolean>): Int {
//    var pixels = 0
//
//    if (row > 0) {
//        if (column > 0) {
//            if (image[row - 1, column - 1]) {
//                ++pixels
//            }
//        }
//        if (column < image.numberOfColumns - 1) {
//            if (image[row - 1, column + 1]) {
//                ++pixels
//            }
//        }
//        if (image[row - 1, column]) {
//            ++pixels
//        }
//    }
//
//    if (row < image.numberOfRows - 1) {
//        if (column > 0) {
//            if (image[row + 1, column - 1]) {
//                ++pixels
//            }
//        }
//        if (column < image.numberOfColumns - 1) {
//            if (image[row + 1, column + 1]) {
//                ++pixels
//            }
//        }
//        if (image[row + 1, column]) {
//            ++pixels
//        }
//    }
//
//    if (column > 0) {
//        if (image[row, column - 1]) {
//            ++pixels
//        }
//    }
//    if (column < image.numberOfColumns - 1) {
//        if (image[row, column + 1]) {
//            ++pixels
//        }
//    }
//
//    if (image[row, column]) {
//        ++pixels
//    }
//
//    return pixels
//}
//
//fun makeThin(image: Array<BooleanArray>): Matrix<Boolean> = _root_ide_package_.com.kjipo.representation.raster.makeThin(Matrix(image.size, image[0].size, { row, column -> image[row][column] }))
//fun makeThin(image: Matrix<Boolean>) = _root_ide_package_.com.kjipo.representation.raster.thin(_root_ide_package_.com.kjipo.representation.raster.fillIsolatedHoles(image))
//fun fillIsolatedHoles(image: Matrix<Boolean>): Matrix<Boolean> {
//    val result = Matrix.copy(image)
//
//    for (row in 0 until image.numberOfRows) {
//        for (column in 0 until image.numberOfColumns) {
//            if (result[row, column]) {
//                continue
//            }
//            result[row, column] = _root_ide_package_.com.kjipo.representation.raster.pixelsFilled(row, column, image) == 8
//        }
//    }
//
//    return result
//}
//
//fun binaryFillHoles(image: Matrix<Boolean>): Matrix<Boolean> {
//    val outputImage = Matrix(image.numberOfRows, image.numberOfColumns, { row, column -> false })
//    val mask = Matrix.copy(image)
//    _root_ide_package_.com.kjipo.representation.raster.invertImage(mask)
//
//    outputImage.forEachIndexed({ row, column, value ->
//        if (row == 0 && mask[row, column]) {
//            outputImage[row, column] = true
//        } else if (row == image.numberOfRows - 1 && mask[row, column]) {
//            outputImage[row, column] = true
//        } else if (column == 0 && mask[row, column]) {
//            outputImage[row, column] = true
//        } else if (column == image.numberOfColumns - 1 && mask[row, column]) {
//            outputImage[row, column] = true
//        }
//    })
//
//    var change = false
//    while (true) {
//        outputImage.forEachIndexed({ row, column, value ->
//            if (outputImage[row, column]) {
//                // Up
//                if (EncodingUtilities.validCoordinates(row - 1, column, image.numberOfRows, image.numberOfColumns)
//                        && mask[row - 1, column]) {
//                    if (!outputImage[row - 1, column]) {
//                        change = true
//                    }
//                    outputImage[row - 1, column] = true
//                }
//                // Down
//                if (EncodingUtilities.validCoordinates(row + 1, column, image.numberOfRows, image.numberOfColumns)
//                        && mask[row + 1, column]) {
//                    if (!outputImage[row + 1, column]) {
//                        change = true
//                    }
//                    outputImage[row + 1, column] = true
//                }
//                // Left
//                if (EncodingUtilities.validCoordinates(row, column - 1, image.numberOfRows, image.numberOfColumns)
//                        && mask[row, column - 1]) {
//                    if (!outputImage[row, column - 1]) {
//                        change = true
//                    }
//                    outputImage[row, column - 1] = true
//                }
//                // Right
//                if (EncodingUtilities.validCoordinates(row, column + 1, image.numberOfRows, image.numberOfColumns)
//                        && mask[row, column + 1]) {
//                    if (!outputImage[row, column + 1]) {
//                        change = true
//                    }
//                    outputImage[row, column + 1] = true
//                }
//            }
//
//        })
//
//        if (!change) {
//            break
//        }
//        change = false
//    }
//
//    return Matrix(image.numberOfRows, image.numberOfColumns, { row, column ->
//        !outputImage[row, column]
//    })
//}
//
//fun invertImage(image: Matrix<Boolean>) {
//    image.forEachIndexed({ row, column, value -> image[row, column] = !value })
//}
//
//fun getNeighbour(row: Int, column: Int, neighbourParam: Int, image: Matrix<Boolean>): Boolean {
//    var neighbour = neighbourParam
//
//    if (neighbour > 8) {
//        neighbour = neighbour % 9 + 1
//    }
//
//    if (neighbour == 1) {
//        if (column == image.numberOfColumns - 1) {
//            return false
//        } else {
//            return image[row, column + 1]
//        }
//    }
//    if (neighbour == 2) {
//        if (row == 0 || column == image.numberOfColumns - 1) {
//            return false
//        } else {
//            return image[row - 1, column + 1]
//        }
//    }
//    if (neighbour == 3) {
//        if (row == 0) {
//            return false
//        } else {
//            return image[row - 1, column]
//        }
//    }
//    if (neighbour == 4) {
//        if (row == 0 || column == 0) {
//            return false
//        } else {
//            return image[row - 1, column - 1]
//        }
//    }
//    if (neighbour == 5) {
//        if (column == 0) {
//            return false
//        } else {
//            return image[row, column - 1]
//        }
//    }
//    if (neighbour == 6) {
//        if (row == image.numberOfRows - 1 || column == 0) {
//            return false
//        } else {
//            return image[row + 1, column - 1]
//        }
//    }
//    if (neighbour == 7) {
//        if (row == image.numberOfRows - 1) {
//            return false
//        } else {
//            return image[row + 1, column]
//        }
//    }
//    if (neighbour == 8) {
//        if (row == image.numberOfRows - 1 || column == image.numberOfColumns - 1) {
//            return false
//        } else {
//            return image[row + 1, column + 1]
//        }
//    }
//
//    throw IllegalStateException("Unexpected neighbour number")
//}
//
//fun getXh(row: Int, column: Int, result: Matrix<Boolean>): Int {
//    var sum = 0
//    for (i in 1..4) {
//        if (!_root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 2 * i - 1, result)
//                && (_root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 2 * i, result)
//                        || _root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 2 * i + 1, result))) {
//            sum += 1
//        }
//    }
//    return sum
//}
//
//fun getN1(row: Int, column: Int, result: Matrix<Boolean>): Int {
//    var sum = 0
//    for (k in 1..4) {
//        if (_root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 2 * k - 1, result)) {
//            sum += 1
//        }
//    }
//    return sum
//}
//
//fun getN2(row: Int, column: Int, result: Matrix<Boolean>): Int {
//    var sum = 0
//    for (k in 1..4) {
//        if (_root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 2 * k, result)
//                || _root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 2 * k + 1, result)) {
//            sum += 1
//        }
//    }
//    return sum
//}
//
///**
// * TODO This method does not work as it should
// */
//fun thinNotWorking(image: Matrix<Boolean>): Matrix<Boolean> {
//    val result = Matrix.copy(image)
//    var first = true
//    var deletion = true
//
//    val toDelete = mutableListOf<Pair<Int, Int>>()
//
//    while (deletion) {
//        deletion = false
//
//        for (row in 0 until image.numberOfRows) {
//            for (column in 0 until image.numberOfColumns) {
//                if (!result[row, column]) {
//                    continue
//                }
//
//                val xH = _root_ide_package_.com.kjipo.representation.raster.getXh(row, column, result)
//
//                if (xH != 1) {
//                    continue
//                }
//
//                val n1 = _root_ide_package_.com.kjipo.representation.raster.getN1(row, column, result)
//                val n2 = _root_ide_package_.com.kjipo.representation.raster.getN2(row, column, result)
//
//                val nMin = _root_ide_package_.kotlin.math.min(n1, n2)
//
//                if (nMin < 2 || nMin > 3) {
//                    continue
//                }
//
//                if (first) {
//                    first = false
//                    if (!(_root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 2, result)
//                                    || _root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 3, result)
//                                    || !_root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 8, result)
//                                    ) && _root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 1, result)) {
//                        continue
//                    }
//                } else {
//                    if (!(_root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 6, result)
//                                    || _root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 7, result)
//                                    || !_root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 4, result))
//                            && _root_ide_package_.com.kjipo.representation.raster.getNeighbour(row, column, 5, result)) {
//                        continue
//                    }
//                }
//
//
//                toDelete.add(Pair(row, column))
//                deletion = true
//
//
//            }
//        }
//
//
//
//        toDelete.forEach {
//            result[it.first, it.second] = false
//        }
//        toDelete.clear()
//    }
//
//
//    return result
//}
//
//internal val lookup1 = booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, true, true, false, false, true, true,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, false, true, true, false, false, false, false, false, false, false, true,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, true, false, false, false, true,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, true, false, false, false, true,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, false, true, true, false, false, false, false, false, false, false, true,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, false, true, true, false, false, false, false, false, false, false, false,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, true, true, false, true, false, false, false, true,
//        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, true, false, true, true, true, true, false, false, true, true, false, false)
//internal val lookup2 = booleanArrayOf(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, true, true, true, false, true, false, true, true, false, false, false, false, true, false,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, true, true, false, false, false, false, false, true, true, false, false, true, false,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, true, true, true, true, false, false, false, true, true, false, true,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, false, true, true, true, false, true, false, true,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, true, false, true, false, false, true, true, true, true, true, true,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, true, true, false, false, true, true, false, true,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, true, true, false, true, false, false, true, false, true, true, false, true,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, true, true, false, true, false, true, true, true, false, true, false, true, false, true,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, true, false, true, false, true, false, true, true, true, true, true, true, true,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, true, false, true, false, false, true, false, true, true, true, true,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, true, false, true, false, false, true, false, true, true, true, true,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true,
//        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true)
//
//fun thin(image: Matrix<Boolean>): Matrix<Boolean> {
//    var previous = image
//    var result: Matrix<Boolean>
//    while (true) {
//        result = Matrix(image.numberOfRows, image.numberOfColumns, { row, column -> false })
//        _root_ide_package_.com.kjipo.representation.raster.applyLookup(_root_ide_package_.com.kjipo.representation.raster.applyLookup(previous, _root_ide_package_.com.kjipo.representation.raster.lookup1), _root_ide_package_.com.kjipo.representation.raster.lookup2).forEachIndexed({ row, column, value ->
//            result[row, column] = previous[row, column] && value
//        })
//
//        var changed = false
//        result.forEachIndexed({ row, column, value ->
//            if (value != previous[row, column]) {
//                changed = true
//                return@forEachIndexed
//            }
//        })
//
//        if (changed) {
//            previous = result
//        } else {
//            return result
//        }
//    }
//}
//
//fun applyLookup(image: Matrix<Boolean>, lookup: BooleanArray): Matrix<Boolean> {
//    if (image.numberOfRows != image.numberOfColumns) {
//        throw IllegalArgumentException("Only square matrices supported")
//    }
//    return applyLookup3(image, lookup)
//}
//
//fun applyLookup3(image: Matrix<Boolean>, lookup: BooleanArray): Matrix<Boolean> {
//    val result = Matrix.copy(image)
//    image.forEachIndexed({ row, column, value ->
//        run {
//            // https://se.mathworks.com/help/images/ref/applylut.html
//            var index = if (value) {
//                16
//            } else {
//                0
//            }
//            FlowDirection.values().forEach { value2 ->
//                index += if (EncodingUtilities.validCell(row, column, value2, image.numberOfRows, image.numberOfColumns)) {
//                    if (!image[row + value2.rowShift, column + value2.columnShift]) {
//                        0
//                    } else {
//                        when (value2) {
//                            FlowDirection.EAST -> 2
//                            FlowDirection.NORTH_EAST -> 4
//                            FlowDirection.NORTH -> 32
//                            FlowDirection.NORTH_WEST -> 256
//                            FlowDirection.WEST -> 128
//                            FlowDirection.SOUTH_WEST -> 64
//                            FlowDirection.SOUTH -> 8
//                            FlowDirection.SOUTH_EAST -> 1
//                        }
//                    }
//                } else {
//                    0
//                }
//            }
//            result[row, column] = lookup[index]
//        }
//
//    })
//    return result
//}
//
//fun transformToBooleanArrays(image: Matrix<Boolean>): Array<BooleanArray> {
//    return (0 until image.numberOfRows).map {
//        val row = it
//        (0 until image.numberOfColumns).map {
//            image[row, it]
//        }.toBooleanArray()
//
//    }.toTypedArray()
//}


fun makeThin(image: Array<BooleanArray>): Matrix<Boolean> =
    makeThin(Matrix(image.size, image[0].size, { row, column -> image[row][column] }))

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


fun isEnd(row: Int, column: Int, image: Matrix<Boolean>): Boolean {
    return if (!image[row, column]) {
        false
    } else {
        pixelsFilled(row, column, image) == 2
    }
}


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


internal val lookup1 = booleanArrayOf(
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    false,
    true,
    true,
    false,
    false,
    true,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    true,
    false,
    false,
    true,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    true,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    true,
    false,
    true,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    true,
    true,
    false,
    true,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    true,
    false,
    true,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    true,
    false,
    true,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    false,
    false,
    true,
    false,
    true,
    true,
    true,
    true,
    false,
    false,
    true,
    true,
    false,
    false
)

internal val lookup2 = booleanArrayOf(
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    false,
    true,
    true,
    true,
    false,
    true,
    false,
    true,
    true,
    false,
    false,
    false,
    false,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    false,
    false,
    true,
    true,
    false,
    false,
    false,
    false,
    false,
    true,
    true,
    false,
    false,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    false,
    false,
    false,
    false,
    true,
    true,
    true,
    true,
    false,
    false,
    false,
    true,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    true,
    true,
    true,
    false,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    true,
    false,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    false,
    true,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    false,
    false,
    true,
    true,
    false,
    true,
    false,
    false,
    true,
    false,
    true,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    true,
    true,
    true,
    false,
    true,
    false,
    true,
    true,
    true,
    false,
    true,
    false,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    false,
    true,
    false,
    true,
    false,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    false,
    true,
    false,
    true,
    false,
    false,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    false,
    false,
    true,
    false,
    true,
    false,
    false,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    false,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true
)


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
                index += if (EncodingUtilities.validCell(
                        row,
                        column,
                        value2,
                        image.numberOfRows,
                        image.numberOfColumns
                    )
                ) {
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

    matrix.forEachIndexed { row, column, value ->
        if (value) {
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
    }

    val occupiedRows = maxRow - minRow
    val occupiedColumns = maxColumn - minColumn
    val sideSize = if (occupiedRows < occupiedColumns) {
        occupiedColumns
    } else {
        occupiedRows
    }

    return Matrix(sideSize, sideSize) { row, column ->
        val offsetRow = row + minRow
        val offsetColumn = column + minColumn

        if (offsetRow >= matrix.numberOfRows || offsetColumn >= matrix.numberOfColumns) {
            false
        } else {
            matrix[offsetRow, offsetColumn]
        }
    }
}


inline fun <reified T> makeSquare(matrix: Matrix<T>, thresholdFunction: (T) -> Boolean, emptyValue: T): Matrix<T> {
    if (matrix.numberOfRows == matrix.numberOfColumns) {
        return Matrix.copy(matrix)
    }

    var minRow = 0
    var maxRow = 0
    var minColumn = 0
    var maxColumn = 0

    matrix.forEachIndexed { row, column, value ->
        if (thresholdFunction(matrix[row, column])) {
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
    }

    val occupiedRows = maxRow - minRow
    val occupiedColumns = maxColumn - minColumn
    val sideSize = if (occupiedRows < occupiedColumns) {
        occupiedColumns
    } else {
        occupiedRows
    }

    return Matrix(sideSize, sideSize) { row, column ->
        val offsetRow = row + minRow
        val offsetColumn = column + minColumn

        if (offsetRow >= matrix.numberOfRows || offsetColumn >= matrix.numberOfColumns) {
            emptyValue
        } else {
            matrix[offsetRow, offsetColumn]
        }
    }
}


fun bwmorphEndpoints(image: Matrix<Boolean>): Matrix<Boolean> {
    // TODO Does not mark everything that looks like an endpoint. Try to find a better algorithm

    val result = Matrix(image.numberOfRows, image.numberOfColumns, { row, column -> false })

    for (row in 0 until image.numberOfRows) {
        for (column in 0 until image.numberOfColumns) {
            result[row, column] = isEnd(row, column, image)
        }
    }

    return result
}


fun getNeighbourhood(matrix: Matrix<Boolean>, center: Pair<Int, Int>): Matrix<Boolean> {
    return getNeighbourhood(matrix, center.first, center.second)
}

fun getNeighbourhood(matrix: Matrix<Boolean>, row: Int, column: Int): Matrix<Boolean> {
    val result = Matrix(3, 3) { _, _ -> false }
    result[1, 1] = matrix[row, column]
    FlowDirection.entries.forEach {
        if (EncodingUtilities.validCell(row, column, it, matrix.numberOfRows, matrix.numberOfColumns)) {
            result[1 + it.rowShift, 1 + it.columnShift] = matrix[row + it.rowShift, column + it.columnShift]
        }
    }
    return result
}

fun <T> getNeighbourhood(
    matrix: Matrix<T>,
    row: Int,
    column: Int,
    directionList: Array<FlowDirection> = FlowDirection.entries.toTypedArray()
): List<Pair<FlowDirection, Boolean>> {
    return directionList.map {
        Pair(it, EncodingUtilities.validCell(row, column, it, matrix.numberOfRows, matrix.numberOfColumns))
    }
}

fun getFlowDirectionArray(): Array<FlowDirection> {
    return arrayOf(
        FlowDirection.EAST,
        FlowDirection.SOUTH_EAST,
        FlowDirection.NORTH_EAST,
        FlowDirection.SOUTH,
        FlowDirection.SOUTH_WEST,
        FlowDirection.WEST,
        FlowDirection.NORTH_WEST,
        FlowDirection.NORTH,
    )
}

fun <T> getNeighbourhoodType(matrix: Matrix<T>, row: Int, column: Int, valueFunction: (T) -> Boolean): Matrix<Boolean> {
    return Matrix(3, 3) { row2, column2 ->
        if (row2 == 1 && column2 == 1) {
            valueFunction(matrix[row, column])
        } else {
            val rowOffset = row2 - 1
            val columnOffset = column2 - 1

            getFlowDirectionForOffset(rowOffset, columnOffset)!!.let {
                EncodingUtilities.validCell(
                    row + rowOffset,
                    column + columnOffset,
                    it,
                    matrix.numberOfRows,
                    matrix.numberOfColumns
                ) && valueFunction(matrix[row + rowOffset, column + columnOffset])
            }
        }
    }
}


inline fun <reified T> scaleMatrix(
    matrix: Matrix<T>,
    newNumberOfRows: Int,
    newNumberOfColumns: Int
): Matrix<T> {
    val scaleRow = matrix.numberOfRows / newNumberOfRows.toDouble()
    val scaleColumn = matrix.numberOfColumns / newNumberOfColumns.toDouble()

    return Matrix<T>(newNumberOfRows, newNumberOfColumns) { row, column ->
        val lookupRow: Int = kotlin.math.floor(row * scaleRow).toInt()
        val lookupColumn: Int = kotlin.math.floor(column * scaleColumn).toInt()

        matrix[lookupRow, lookupColumn]
    }
}
