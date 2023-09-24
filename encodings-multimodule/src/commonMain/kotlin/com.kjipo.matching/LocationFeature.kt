package com.kjipo.matching


sealed class LocationFeature {

    abstract val id: Int
}


data class EndpointFeature(override val id: Int, val location: Pair<Int, Int>): LocationFeature()

