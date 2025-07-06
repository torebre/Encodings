package com.kjipo.experiments


class LineSegment(val id: Int, val straightLineLength: List<Pair<Int, Int>>) {

    fun incline(): Double {
        val firstPoint = straightLineLength.first()
        val lastPoint = straightLineLength.last()

        return (lastPoint.second - firstPoint.second).toDouble() / (lastPoint.first - firstPoint.first)
    }

    fun lineSummary(): String {
        val firstPoint = straightLineLength.first()
        val lastPoint = straightLineLength.last()

        return "Line segment $id: from $firstPoint to $lastPoint"
    }

}