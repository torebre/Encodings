package com.kjipo.experiments

import com.kjipo.representation.raster.FlowDirection


class Stroke(val path: List<PathPoint>) {

}

class PathPoint(val row: Int, val column: Int, val direction: FlowDirection) {

}