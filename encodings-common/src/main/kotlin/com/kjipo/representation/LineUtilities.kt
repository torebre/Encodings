package com.kjipo.representation

import kotlin.math.abs
import kotlin.math.min

object LineUtilities {


    fun createLine(startX: Int, startY: Int, stopX: Int, stopY: Int): Matrix<Int?> {

//    if(start.x == stop.x) {
//        # Horizontal line
//                rows <- abs(stop.y - start.y) + 1
//        result <- matrix(nrow = rows, ncol = 2)
//        if(start.y < stop.y) {
//            result[ ,1] <- rep(start.x, rows)
//            result[ ,2] <- start.y:stop.y
//        }
//        else {
//            result[ ,1] <- rep(start.x, rows)
//            result[ ,2] <- stop.y:start.y
//        }
//
//        return(result)
//    }

        var rows = 0

                if(startX == stopX) {
            // Horizontal line
            rows = abs(stopY - startY) + 1
                    var result = Matrix<Int>(rows, 2)

                    if(startY < stopY) {
                        var counter = 0
                        for(i in 0 until rows) {
                            result[counter, 1] = startX
                            result[counter, 2] = startY + i
                            ++counter
                        }
                    }
                    else {
                        var counter = 0
                        for(i in 0 until rows) {
                            result[counter, 1] = startX
                            result[counter, 2] = stopY - i
                        }
                    }
                    return result
        }

//        if (start.y == stop.y) {
//            # Vertical line
//                    rows < -abs(stop.x - start.x) + 1
//            result < -matrix(nrow = rows, ncol = 2)
//            if (start.x < stop.x) {
//                result[,1] < -start.x:stop.x
//                result[,2] < -rep(start.y, rows)
//            } else {
//                result[,1] < -stop.x:start.x
//                result[,2] < -rep(start.y, rows)
//            }
//
//            return (result)
//        }


        if(startY == stopY) {
            // Vertical line
            var rows = -abs(stopX - startX)  + 1
            var result = Matrix<Int>(rows, 2)

            if(startX < stopX) {
                var counter = 0
                for(i in startX until stopX) {
                    result[counter, 1] = i
                    result[counter, 2] = -startY
                    ++counter
                }
            }
            else {
                var counter = 0
                for(i in stopX until stopY) {
                    result[counter, 1] = -i
                    result[counter, 2] = -startY
                }
            }
        }

//        swap < -stop.x < start.x
//        first.translate < -abs(min(0, min(start.x, stop.x)))
//        second.translate < -abs(min(0, min(start.y, stop.y)))

        var swap = -stopX < startX
        var firstTranslate = abs(min(0, min(startX, stopX)))
        var secondTranslate = abs(min(0, min(startY, stopY)))


//        if (swap) {
//            start.x.translate < -stop.x + first.translate
//            start.y.translate < -stop.y + second.translate
//            stop.x.translate < -start.x + first.translate
//            stop.y.translate < -start.y + second.translate
//        } else {
//            start.x.translate < -start.x + first.translate
//            start.y.translate < -start.y + second.translate
//            stop.x.translate < -stop.x + first.translate
//            stop.y.translate < -stop.y + second.translate
//        }

        var startXTranslate:Int
        var startYTranslate: Int
        var stopXTranslate: Int
        var stopYTranslate: Int

        if(swap) {
            startXTranslate = stopX + firstTranslate
            startYTranslate = stopY + secondTranslate
            stopXTranslate = startX + firstTranslate
            stopYTranslate = startY + secondTranslate
        }
        else {
            startXTranslate = startX + firstTranslate
            startYTranslate = startY + secondTranslate
            stopXTranslate = stopX + firstTranslate
            stopYTranslate = stopY + secondTranslate
        }

//        x.delta < -stop.x.translate - start.x.translate
//        y.delta < -stop.y.translate - start.y.translate
//        delta.error < -abs(y.delta / x.delta)
//        if (y.delta < 0) {
//            sign.y.delta < --1
//        } else {
//            sign.y.delta < -1
//        }


        var xDelta = stopXTranslate - startXTranslate
        var yDelta = stopYTranslate - startYTranslate
        var deltaError = abs(yDelta.toDouble() / xDelta)
        val signYDelta = if(yDelta < 0) {
            -1
        }
        else {
            1
        }


//        error < -0
//        y < -start.y.translate
//        new.y = y

        var error = 0
        var y = startYTranslate
        var newY = y


//        # print(paste('start.x', start.x, 'stop.x', stop.x, 'start.y', start.y, 'stop.y', stop.y))

//        temp.result < -matrix(nrow = 2 * (abs(start.x - stop.x) + abs(start.y - stop.y)), ncol = 2)
//        counter < -1

        var tempResult = Matrix<Int>(2 * abs(startX - stopX) + abs(startY - stopY), 2)
        var counter = 1

//        for (x in start.x.translate:stop.x.translate) {
//            if (y != new.y) {
//                if (sign.y.delta < 0) {
//                    for (inc. y in new.y:y) {
//                        temp.result[counter, 1] < -x
//                        temp.result[counter, 2] < -inc.y
//                        counter < -counter + 1
//                    }
//                } else {
//                    for (inc. y in y:new.y) {
//                        temp.result[counter, 1] < -x
//                        temp.result[counter, 2] < -inc.y
//                        counter < -counter + 1
//                    }
//                }
//            } else {
//                temp.result[counter, 1] < -x
//                temp.result[counter, 2] < -y
//                counter < -counter + 1
//            }
//
//            y < -new.y
//
//            error < -error + delta.error
//            while (error >= 0.5) {
//                new.y < -new.y + sign.y.delta
//                error < -error - 1
//            }
//        }

        for(x in startXTranslate until stopXTranslate) {
            if(y != newY) {
                if(signYDelta < 0) {
                    for(incY in newY until y) {
                        tempResult[counter, 1] = x
                        tempResult[counter, 2] = incY
                        ++counter
                    }
                }
                else {
                    for(incY in y until newY) {
                        tempResult[counter, 1] = x
                        tempResult[counter, 2] = incY
                        ++counter
                    }
                }

                y = newY
                var error = error + deltaError
                while(error >= 0.5) {
                    newY = newY + signYDelta
                    --error
                }
            }
        }


//        temp.result < -temp.result[which(!(temp.result[, 1] % in % NA)), ]
//        temp.result[, 1] < -temp.result[, 1] - first.translate
//        temp.result[, 2] < -temp.result[, 2] - second.translate


        val rowsToInclude = mutableListOf<Int>()
        for(i in 0 until tempResult.numberOfRows) {
            if(tempResult[i, 0] != null) {
                rowsToInclude.add(i)
            }
        }

        val tempMatrix = Matrix(rowsToInclude.size, 2) { row, column ->
            tempResult[rowsToInclude[row], column]
        }
        for(i in 0 until tempResult.numberOfRows) {
            tempMatrix[i, 0] = tempMatrix[i, 0]!! - firstTranslate
            tempMatrix[i, 1] = tempMatrix[i, 1]!! - secondTranslate
        }

//        if (swap) {
//            temp.result[, 1] < -rev(temp.result[, 1])
//            temp.result[, 2] < -rev(temp.result[, 2])
//        }

//        return (temp.result)

        return if(swap) {
            for(i in 0 until tempMatrix.numberOfRows / 2) {
                var temp = tempMatrix[i, 0]
                tempMatrix[i, 0] = tempMatrix[tempMatrix.numberOfRows - 1, 0]
                tempMatrix[tempMatrix.numberOfRows - 1, 0] = temp

                temp = tempMatrix[i, 1]
                tempMatrix[i, 0] = tempMatrix[tempMatrix.numberOfRows - 1, 1]
                tempMatrix[tempMatrix.numberOfRows - 1, 1] = temp
            }
            tempMatrix
        }
        else {
            tempMatrix
        }
    }


}