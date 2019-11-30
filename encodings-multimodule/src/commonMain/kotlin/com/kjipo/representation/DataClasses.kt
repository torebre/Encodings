package com.kjipo.representation


open class Line(val lineNumber: Int, val angle: Double, val length: Double, val startX: Int, val startY: Int) {

    override fun toString() = "$lineNumber,$angle,$length,$startX,$startY"

}

class SegmentLine(val unicode: Int, lineNumber: Int, angle: Double, length: Double, startX: Int, startY: Int, val segment: Int) : Line(lineNumber, angle, length, startX, startY)


class Boundary(val xMin: Int, val yMin: Int, val xMax: Int, val yMax: Int)
