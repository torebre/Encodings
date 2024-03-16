package com.kjipo.experiments

import com.kjipo.matching.EndpointFeature
import com.kjipo.matching.EndpointTriplet
import kotlin.math.pow
import kotlin.math.sqrt


class RelationDataForImage(
    val endpointsRelationData: List<EndpointFeature>,
    val relationData: Map<Pair<Int, Int>, PointsTest.EndpointsRelationData>
) {


    fun computeRelationsForEndpoint(endpointFeature: EndpointFeature): EndpointTriplet? {
        val closestPointsData = getClosestPoints(endpointFeature, 2)

        if(closestPointsData.size < 2) {
            return null
        }

        val otherPoint1 = if(closestPointsData[0].endpoint1 == endpointFeature) closestPointsData[0].endpoint2 else closestPointsData[0].endpoint1
        val otherPoint2 = if(closestPointsData[1].endpoint1 == endpointFeature) closestPointsData[1].endpoint2 else closestPointsData[1].endpoint1

        val normalizedDirectionVector1 = Pair(endpointFeature.location.first - otherPoint1.location.first, endpointFeature.location.second - otherPoint1.location.second).let {
            val length = sqrt(it.first.toDouble().pow(2) + it.second.toDouble().pow(2))

            Pair(it.first.toDouble() / length, it.second.toDouble() / length)
        }
        val normalizedDirectionVector2 = Pair(endpointFeature.location.first - otherPoint2.location.first, endpointFeature.location.second - otherPoint2.location.second).let {
            val length = sqrt(it.first.toDouble().pow(2) + it.second.toDouble().pow(2))

            Pair(it.first.toDouble() / length, it.second.toDouble() / length)
        }

        val dotProduct = normalizedDirectionVector1.first * normalizedDirectionVector2.first + normalizedDirectionVector1.second * normalizedDirectionVector2.second

        val relativeDistance = if(closestPointsData[0].distance > closestPointsData[1].distance) {
            closestPointsData[1].distance / closestPointsData[0].distance
        }
        else {
            closestPointsData[0].distance / closestPointsData[1].distance
        }

        val center = Pair((endpointFeature.location.first + otherPoint1.location.first + otherPoint2.location.first).toDouble() / 3,
            (endpointFeature.location.second + otherPoint1.location.second + otherPoint2.location.second).toDouble() / 3)

        return EndpointTriplet(endpointFeature, center, dotProduct, relativeDistance)
    }

    fun computeRelationsForEndpoints() {
        endpointsRelationData.forEach { computeRelationsForEndpoint(it) }
    }


    fun getClosestPoints(endpointFeature: EndpointFeature, numberOfPointsToGet: Int): List<PointsTest.EndpointsRelationData> {
       return relationData.filter { it.key.first == endpointFeature.id || it.key.second == endpointFeature.id }
            .map { it.value }
            .sortedBy { it.distance }
            .take(numberOfPointsToGet)
    }

    override fun toString(): String {
        return "RelationDataForImage(endpointsRelationData=$endpointsRelationData, relationData=$relationData)"
    }


}