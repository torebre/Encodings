package com.kjipo.representation.raster

import kotlin.math.PI


enum class FlowDirection(val rowShift: Int, val columnShift: Int, val angleInRadians: Double) {
    EAST(0, 1, 0.0), NORTH_EAST(-1, 1, PI / 4), NORTH(-1, 0, PI / 2), NORTH_WEST(-1, -1, 3 * PI / 4), WEST(0, -1, PI), SOUTH_WEST(1, -1, 5 * PI / 4), SOUTH(1, 0, 3 * PI / 2), SOUTH_EAST(1, 1, 7 * PI / 4);

}