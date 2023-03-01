package com.kjipo.representation.prototype2


class EndpointPrototype(imageIdentifier: String, val point: Pair<Int, Int>): AppliedPrototype(imageIdentifier) {

    constructor(imageIdentifier: String, x: Int, y: Int): this(imageIdentifier, Pair(x, y))

}