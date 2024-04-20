package com.kjipo.representation.prototype

import com.kjipo.representation.segment.Segment


interface Prototype {
    fun getSegments(): List<Segment>
}