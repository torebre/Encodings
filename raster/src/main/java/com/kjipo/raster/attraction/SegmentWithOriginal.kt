package com.kjipo.raster.attraction

import com.kjipo.representation.segment.Pair
import com.kjipo.representation.segment.Segment

class SegmentWithOriginal(
    val originalData: List<Pair>, override val pairs: List<Pair>, val rotationAngle: Double,
    val squareSide: Int, val numberOfRows: Int, val numberOfColumns: Int, val moveOperation: MoveOperation
) : Segment