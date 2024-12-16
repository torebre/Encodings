package com.kjipo.experiments

import com.kjipo.representation.raster.FlowDirection


class Stroke(val path: List<PathPoint>) {

}

sealed class PathPoint(val row: Int, val column: Int) {

    class PathPointWithDirection(row: Int, column: Int, val direction: FlowDirection): PathPoint(row, column)

}
