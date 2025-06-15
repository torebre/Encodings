package com.kjipo.experiments


data class AreaExtract(val center: Point, val points: List<Point>) {

    constructor(points: List<Point>) : this(findMassCenter(points), points)


    companion object {

        private fun findMassCenter(points: List<Point>): Point {
            if (points.isEmpty()) {
                throw IllegalArgumentException("No points given")
            }

            var rowSum = 0
            var columnSum = 0

            for (point in points) {
                rowSum += point.row
                columnSum += point.column
            }

            return Point(rowSum / points.size, columnSum / points.size)
        }

    }

}
