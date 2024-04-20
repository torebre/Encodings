package com.kjipo.representation.segment


class SegmentImpl : Segment {
    override var pairs: List<Pair> = mutableListOf()
        private set

    constructor() {}

    constructor(pairs: List<Pair>) {
        this.pairs = pairs
    }

    override fun toString(): String {
        return "SegmentImpl{" +
                "pairs=" + pairs +
                '}'
    }
}