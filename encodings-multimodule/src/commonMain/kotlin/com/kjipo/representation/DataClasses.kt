package com.kjipo.representation


class Line(val lineNumber: Int, val angle: Double, val length: Double, val startX: Int, val startY: Int) {

    override fun toString() = "$lineNumber,$angle,$length,$startX,$startY"

}


data class Boundary(val xMin: Int, val yMin: Int, val xMax: Int, val yMax: Int)