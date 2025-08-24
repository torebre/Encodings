package com.kjipo.experiments

import com.kjipo.representation.Matrix

data class JoinedSegmentData(val lineSegments: List<LineSegment>, val segmentMatrix: Matrix<Int>)
