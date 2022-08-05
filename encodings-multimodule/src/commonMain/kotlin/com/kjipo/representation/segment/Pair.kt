package com.kjipo.representation.segment

data class Pair(val row: Int, val column: Int) {

    override fun toString(): String {
        return "Pair{" +
                "row=" + row +
                ", column=" + column +
                '}'
    }

    companion object {
        fun of(row: Int, column: Int): Pair {
            return Pair(row, column)
        }
    }
}