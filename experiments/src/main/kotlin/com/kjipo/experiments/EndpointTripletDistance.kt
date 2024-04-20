package com.kjipo.experiments

import com.kjipo.matching.EndpointTriplet

class EndpointTripletDistance(val endpointTriplet: EndpointTriplet, val endpointTriplet2: EndpointTriplet, val distance: Double) {

    override fun toString(): String {
        return "EndpointTripletDistance(endpointTriplet=$endpointTriplet, endpointTriplet2=$endpointTriplet2, distance=$distance)"
    }
}