package com.kjipo


class Line(val lineNumber: Int, val angle: Double, val length: Double, val startX: Int, val startY: Int) {

    override fun toString() = "$lineNumber,$angle,$length,$startX,$startY"

}
