package com.kjipo.matching


sealed class LocationFeature {

}


data class EndpointFeature(val id: Int, val location: Pair<Int, Int>): LocationFeature()

data class EndpointTriplet(
    val mainPoint: EndpointFeature,
    val center: Pair<Double, Double>,
    val dotProduct: Int,
    val relativeDistance: Double
): LocationFeature()