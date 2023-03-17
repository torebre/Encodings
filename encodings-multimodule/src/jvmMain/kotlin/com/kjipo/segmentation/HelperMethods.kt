package com.kjipo.segmentation

import java.lang.IllegalArgumentException


fun getOffset(value: Int): Int {
    return when (value) {
        0 -> -1
        1 -> 0
        2 -> 1
        else -> throw IllegalArgumentException("Unexpected offset")
    }
}
