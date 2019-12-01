package com.kjipo.representation.line

import com.kjipo.representation.raster.FlowDirection
import kotlin.math.atan2
import kotlin.math.sqrt


//class AngleLine {
//    val id: Int
//    private var startPair: Pair<Int, Int>
//    var length: Double
//    var angle: Double
//        private set
//    var angleOffset = 0.0
//    private val connectedTo: MutableCollection<Int> = HashSet()
//
//    private constructor(id: Int, startPair:  Pair<Int, Int>, length: Double, angle: Double, connectedTo: Collection<Int>) {
//        this.id = id
//        this.startPair = Pair(startPair.first, startPair.second)
//        this.length = length
//        this.angle = angle
//        this.connectedTo.addAll(connectedTo)
//    }
//
//    constructor(id: Int, startPair:  Pair<Int, Int>, length: Double, angle: Double) : this(id, startPair, length, angle, emptySet<Int>()) {}
//
//    constructor(id: Int, startPair:  Pair<Int, Int>, endPair:  Pair<Int, Int>) {
//        this.id = id
//        val xDelta: Int = endPair.second - startPair.second
//        val yDelta: Int = endPair.first- startPair.first
//        this.startPair =  Pair<Int, Int>.of(startPair.first, startPair.second)
//        angle = atan2(yDelta.toDouble(), xDelta.toDouble())
//        length = sqrt(pow(xDelta.toDouble(), 2.0) + java.lang.Math.pow(yDelta.toDouble(), 2.0))
//    }
//
//    constructor(angleLine: AngleLine) {
//        id = angleLine.id
//        startPair =  Pair(angleLine.startPair.first, angleLine.startPair.second)
//        length = angleLine.length
//        angle = angleLine.angle
//        angleOffset = angleLine.angleOffset
//        connectedTo.addAll(angleLine.connectedTo)
//    }
//
//    val segments: List<Any>
//        get() {
//            val newStartPair: Pair<*, *> = Pair<Int, Int>(startPair.first, startPair.second)
//            val endPair:  Pair<Int, Int> = endPair
//            val newEndPair: Pair<*, *> = Pair<Int, Int>(endPair.first, endPair.second)
//            val linePairs: List<Pair<Int, Int>> = computeLine(newStartPair, newEndPair)
//            return listOf(SegmentImpl(linePairs.stream()
//                    .map(java.util.function.Function<Pair<Int, Int>,  Pair<Int, Int>> { kotlinPair: Pair<Int?, Int?> ->  Pair<Int, Int>(kotlinPair.component1(), kotlinPair.component2()) })
//                    .collect(Collectors.toList())))
//        }
//
//    val endPair:  Pair<Int, Int>
//        get() {
//            val xDelta: Double = length * java.lang.Math.cos(angle + angleOffset)
//            val yDelta: Double = length * java.lang.Math.sin(angle + angleOffset)
//            return  Pair<Int, Int>.of(java.lang.Math.round(startPair.first + yDelta) as Int, java.lang.Math.round(startPair.second + xDelta) as Int)
//        }
//
//    fun getStartPair():  Pair<Int, Int> {
//        return  Pair(startPair.first, startPair.second)
//    }
//
//    fun setStartPair(startPair:  Pair<Int, Int>) {
//        java.util.Objects.requireNonNull(startPair)
//        this.startPair =  Pair<Int, Int>.of(startPair.first, startPair.second)
//    }
//
//    fun addAngleOffset(angleOffset: Double) {
//        this.angleOffset += angleOffset
//    }
//
//    fun getConnectedTo(): Collection<Int> {
//        return HashSet(connectedTo)
//    }
//
//    fun addConnectedTo(id: Int) {
//        connectedTo.add(id)
//    }
//
//    private fun movePair(flowDirection: FlowDirection, startPair:  Pair<Int, Int>, endPair:  Pair<Int, Int>, moveStartPair: Boolean): com.kjipo.prototype.AngleLine {
//        return if (moveStartPair) {
//            com.kjipo.prototype.AngleLine(
//                    id,
//                     Pair<Int, Int>.of(startPair.first + flowDirection.getRowShift(),
//                            startPair.second + flowDirection.getColumnShift()),
//                    endPair)
//        } else com.kjipo.prototype.AngleLine(id, startPair,
//                 Pair<Int, Int>.of(endPair.first + flowDirection.getRowShift(),
//                        endPair.second + flowDirection.getColumnShift()))
//    }
//
//    override fun equals(o: Any?): Boolean {
//        if (this === o) return true
//        if (o == null || javaClass != o.javaClass) return false
//        val angleLine: com.kjipo.prototype.AngleLine = o as com.kjipo.prototype.AngleLine
//        return id == angleLine.id && java.lang.Double.compare(angleLine.length, length) == 0 && java.lang.Double.compare(angleLine.angle, angle) == 0 && java.lang.Double.compare(angleLine.angleOffset, angleOffset) == 0 &&
//                java.util.Objects.equals(startPair, angleLine.startPair) &&
//                java.util.Objects.equals(connectedTo, angleLine.connectedTo)
//    }
//
//    override fun hashCode(): Int {
//        return java.util.Objects.hash(id, startPair, length, angle, angleOffset, connectedTo)
//    }
//
//    override fun toString(): String {
//        return "AngleLine{" +
//                "id=" + id +
//                ", startPair=" + startPair +
//                ", length=" + length +
//                ", angle=" + angle +
//                ", angleOffset=" + angleOffset +
//                ", connectedTo=" + connectedTo +
//                ", endPair=" + endPair +
//                '}'
//    }
//}