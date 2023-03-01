package com.kjipo.matching

import com.kjipo.representation.prototype2.LinePrototype
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class LengthDifference(private val linePrototype: LinePrototype, private val linePrototype2: LinePrototype) :
    AppliedRelation() {

    fun getAbsoluteLengthDifference(): Double {
        return abs(
            sqrt(
                (linePrototype.startPoint.point.first - linePrototype.stopPoint.point.first).toDouble().pow(2)
                        + (linePrototype.startPoint.point.second - linePrototype.stopPoint.point.second).toDouble()
                    .pow(2)
            )
                    - sqrt(
                (linePrototype.startPoint.point.first - linePrototype.stopPoint.point.first).toDouble().pow(2)
                        + (linePrototype.startPoint.point.second - linePrototype.stopPoint.point.second).toDouble()
                    .pow(2)
            )
        )
    }


}