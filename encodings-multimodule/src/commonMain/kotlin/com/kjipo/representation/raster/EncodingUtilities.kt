package com.kjipo.representation.raster


import kotlin.jvm.JvmStatic

object EncodingUtilities {
    @JvmStatic
    fun copyRaster(raster: Array<BooleanArray>): Array<BooleanArray> {
        val result = Array(raster.size) { BooleanArray(raster[0].size) }
        for (row in raster.indices) {
            for (column in 0 until raster[0].size) {
                result[row][column] = raster[row][column]
            }
        }
        return result
    }

    @JvmStatic
    fun determineNeighbourTypes(row: Int, column: Int, raster: Array<BooleanArray>): Array<TileType?> {
        val result = arrayOfNulls<TileType>(8)
        if (column == raster[0].size - 1) {
            result[0] = TileType.OUTSIDE_SCREEN
        } else if (!raster[row][column + 1]) {
            result[0] = TileType.OUTSIDE_CHARACTER
        } else {
            result[0] = TileType.OPEN
        }
        if (row == 0 || column == raster[0].size - 1) {
            result[1] = TileType.OUTSIDE_SCREEN
        } else if (!raster[row - 1][column + 1]) {
            result[1] = TileType.OUTSIDE_CHARACTER
        } else {
            result[1] = TileType.OPEN
        }
        if (row == 0) {
            result[2] = TileType.OUTSIDE_SCREEN
        } else if (!raster[row - 1][column]) {
            result[2] = TileType.OUTSIDE_CHARACTER
        } else {
            result[2] = TileType.OPEN
        }
        if (row == 0 || column == 0) {
            result[3] = TileType.OUTSIDE_SCREEN
        } else if (!raster[row - 1][column - 1]) {
            result[3] = TileType.OUTSIDE_CHARACTER
        } else {
            result[3] = TileType.OPEN
        }
        if (column == 0) {
            result[4] = TileType.OUTSIDE_SCREEN
        } else if (!raster[row][column - 1]) {
            result[4] = TileType.OUTSIDE_CHARACTER
        } else {
            result[4] = TileType.OPEN
        }
        if (row == raster.size - 1 || column == 0) {
            result[5] = TileType.OUTSIDE_SCREEN
        } else if (!raster[row + 1][column - 1]) {
            result[5] = TileType.OUTSIDE_CHARACTER
        } else {
            result[5] = TileType.OPEN
        }
        if (row == raster.size - 1) {
            result[6] = TileType.OUTSIDE_SCREEN
        } else if (!raster[row + 1][column]) {
            result[6] = TileType.OUTSIDE_CHARACTER
        } else {
            result[6] = TileType.OPEN
        }
        if (row == raster.size - 1 || column == raster[0].size - 1) {
            result[7] = TileType.OUTSIDE_SCREEN
        } else if (!raster[row + 1][column + 1]) {
            result[7] = TileType.OUTSIDE_CHARACTER
        } else {
            result[7] = TileType.OPEN
        }
        return result
    }

    @JvmStatic
    fun validCell(row: Int, column: Int, flowDirection: FlowDirection, rows: Int, columns: Int): Boolean {
        return validCoordinates(row + flowDirection.rowShift,
                column + flowDirection.columnShift, rows, columns)
    }

    @JvmStatic
    fun validCoordinates(shiftedRow: Int, shiftedColumn: Int, rows: Int, columns: Int): Boolean {
        if (shiftedRow < 0 || shiftedRow >= rows) {
            return false
        }
        return !(shiftedColumn < 0 || shiftedColumn >= columns)
    }

    @JvmStatic
    fun validCellOppositeDirection(row: Int, column: Int, flowDirection: FlowDirection, rows: Int, columns: Int): Boolean {
        val shiftedRow = row - flowDirection.rowShift
        if (shiftedRow < 0 || shiftedRow >= rows) {
            return false
        }
        val shiftedColumn = column - flowDirection.columnShift
        return if (shiftedColumn < 0 || shiftedColumn >= columns) {
            false
        } else true
    }

    @JvmStatic
    fun computeRasterBasedOnPairs(rows: Int, columns: Int, pairs: Collection<Pair<Int, Int>>): Array<BooleanArray> {
        val result = Array(rows) { BooleanArray(columns) }
        pairs.forEach { pair: Pair<Int, Int> -> result[pair.first][pair.second] = true }
        return result
    }

    @JvmStatic
    fun determineOffset(rowOffset: Int, columnOffset: Int): FlowDirection? {
        if (rowOffset == -1 && columnOffset == -1) {
            return FlowDirection.NORTH_WEST
        }
        if (rowOffset == -1 && columnOffset == 0) {
            return FlowDirection.NORTH
        }
        if (rowOffset == -1 && columnOffset == 1) {
            return FlowDirection.NORTH_EAST
        }
        if (rowOffset == 0 && columnOffset == -1) {
            return FlowDirection.WEST
        }
        if (rowOffset == 0 && columnOffset == 0) {
            return null
        }
        if (rowOffset == 0 && columnOffset == 1) {
            return FlowDirection.EAST
        }
        if (rowOffset == 1 && columnOffset == -1) {
            return FlowDirection.SOUTH_WEST
        }
        if (rowOffset == 1 && columnOffset == 0) {
            return FlowDirection.SOUTH
        }
        return if (rowOffset == 1 && columnOffset == 1) {
            FlowDirection.SOUTH_EAST
        } else null
    }

    @JvmStatic
    fun computeDirection(first: Pair<Int, Int>, second: Pair<Int, Int>): FlowDirection? {
        val rowChange = second.first - first.first
        val columnChange = second.second - first.second
        if (rowChange == 0 && columnChange == 1) {
            return FlowDirection.EAST
        }
        if (rowChange == -1 && columnChange == 1) {
            return FlowDirection.NORTH_EAST
        }
        if (rowChange == 1 && columnChange == 1) {
            return FlowDirection.SOUTH_EAST
        }
        if (rowChange == -1 && columnChange == 0) {
            return FlowDirection.NORTH
        }
        if (rowChange == 1 && columnChange == 0) {
            return FlowDirection.SOUTH
        }
        if (rowChange == 0 && columnChange == -1) {
            return FlowDirection.WEST
        }
        if (rowChange == -1 && columnChange == -1) {
            return FlowDirection.NORTH_WEST
        }
        return if (rowChange == 1 && columnChange == -1) {
            FlowDirection.SOUTH_WEST
        } else null
    }
}