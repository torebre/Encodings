package com.kjipo.matching


sealed class LocationFeature


data class EndpointFeature(val id: Int, val location: Pair<Int, Int>) : LocationFeature() {

    override fun toString(): String {
        return "EndpointFeature(id=$id, location=$location)"
    }
}

/**
 * Describes a relation between an endpoint and the two closest other endpoints.
 */
data class EndpointTriplet(
    val mainPoint: EndpointFeature,
    val center: Pair<Double, Double>,
    val dotProduct: Double,
    val relativeDistance: Double
) : LocationFeature()