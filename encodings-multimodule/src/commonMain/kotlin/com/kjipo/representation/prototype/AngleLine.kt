package com.kjipo.representation.prototype

import com.kjipo.representation.raster.FlowDirection
import com.kjipo.representation.segment.Segment
import com.kjipo.representation.segment.SegmentImpl
import com.kjipo.representation.segment.Pair
import com.kjipo.segmentation.computeLine
import kotlin.math.*


data class AngleLine(
    val id: Int,
    var startPair: Pair,
    var length: Double,
    val angle: Double,
    val connectedTo: MutableSet<Int>
) : AdjustablePrototype {
    //    val id: Int
//    private var startPair: com.kjipo.representation.segment.Pair
//    var length: Double
//    var angle: Double
//        private set
    var angleOffset = 0.0
//    private val connectedTo: MutableCollection<Int> = HashSet()

//    private constructor(
//        id: Int,
//        startPair: com.kjipo.representation.segment.Pair,
//        length: Double,
//        angle: Double,
//        connectedTo: Collection<Int>
//    ) {
//        this.id = id
//        this.startPair = com.kjipo.representation.segment.Pair.of(startPair.row, startPair.column)
//        this.length = length
//        this.angle = angle
//        this.connectedTo.addAll(connectedTo)
//    }

    constructor(id: Int, startPair: Pair, length: Double, angle: Double) : this(
        id,
        startPair,
        length,
        angle,
       mutableSetOf()
    )

    constructor(
        id: Int,
        startPair: Pair,
        endPair: Pair
    ) : this(
        id,
        Pair.of(startPair.row, startPair.column),
        sqrt(
            (endPair.column - startPair.column).toDouble().pow(2.0) + (endPair.row - startPair.row).toDouble().pow(2.0)
        ),
        atan2((endPair.row - startPair.row).toDouble(), (endPair.column - startPair.column).toDouble()),
        mutableSetOf()
    )
//    {
//        this.id = id
//        val xDelta = endPair.column - startPair.column
//        val yDelta = endPair.row - startPair.row
//        this.startPair =
//        angle = atan2(yDelta.toDouble(), xDelta.toDouble())
//        length = sqrt(xDelta.toDouble().pow(2.0) + yDelta.toDouble().pow(2.0))
//    }

    constructor(angleLine: AngleLine) : this(
        angleLine.id,
        Pair.of(angleLine.startPair.row, angleLine.startPair.column),
        angleLine.length,
        angleLine.angle,
        angleLine.connectedTo
    ) {
        angleOffset = angleLine.angleOffset
    }

    override fun getMovements(): List<AdjustablePrototype> {
        val endPair = endPair
        return FlowDirection.values()
            .flatMap { flowDirection: FlowDirection ->
                listOf(
                    movePair(flowDirection, startPair, endPair, true),
                    movePair(flowDirection, startPair, endPair, false)
                )
            }
            .filter { angleLine: AngleLine -> angleLine.startPair.row >= 0 && angleLine.startPair.column >= 0 }
            .filter { angleLine: AngleLine -> angleLine.endPair.row >= 0 && angleLine.endPair.column >= 0 }
            .filter { angleLine: AngleLine -> angleLine.startPair != angleLine.endPair }
    }

    override fun getSegments(): List<Segment> {
        val newStartPair = kotlin.Pair(startPair.row, startPair.column)
        val endPair = endPair
        val newEndPair = kotlin.Pair(endPair.row, endPair.column)
        val linePairs = computeLine(newStartPair, newEndPair)
        return listOf(
            SegmentImpl(linePairs.map { kotlinPair ->
                    Pair(kotlinPair.component1(), kotlinPair.component2())
                }.toList()
            )
        )
    }

    val endPair: Pair
        get() {
            val xDelta = length * cos(angle + angleOffset)
            val yDelta = length * sin(angle + angleOffset)
            return Pair.of(
                round(startPair.row + yDelta).toInt(),
                round(startPair.column + xDelta).toInt()
            )
        }

//    fun getStartPair(): Pair {
//        return Pair.of(startPair.row, startPair.column)
//    }

//    fun setStartPair(startPair: Pair) {
//        this.startPair = Pair.of(startPair.row, startPair.column)
//    }

    fun addAngleOffset(angleOffset: Double) {
        this.angleOffset += angleOffset
    }

    fun getConnectedTo(): Collection<Int> {
        return HashSet(connectedTo)
    }

    fun addConnectedTo(id: Int) {
        connectedTo.add(id)
    }

    private fun movePair(
        flowDirection: FlowDirection,
        startPair: Pair,
        endPair: Pair,
        moveStartPair: Boolean
    ): AngleLine {
        return if (moveStartPair) {
            AngleLine(
                id,
                Pair.of(
                    startPair.row + flowDirection.rowShift,
                    startPair.column + flowDirection.columnShift
                ),
                endPair
            )
        } else AngleLine(
            id, startPair,
            Pair.of(
                endPair.row + flowDirection.rowShift,
                endPair.column + flowDirection.columnShift
            )
        )
    }

//    override fun equals(o: Any?): Boolean {
//        if (this === o) return true
//        if (o == null || javaClass != o.javaClass) return false
//        val angleLine = o as AngleLine
//        return id == angleLine.id && java.lang.Double.compare(
//            angleLine.length,
//            length
//        ) == 0 && java.lang.Double.compare(angleLine.angle, angle) == 0 && java.lang.Double.compare(
//            angleLine.angleOffset,
//            angleOffset
//        ) == 0 && startPair == angleLine.startPair && connectedTo == angleLine.connectedTo
//    }
//
//    override fun hashCode(): Int {
//        return Objects.hash(id, startPair, length, angle, angleOffset, connectedTo)
//    }

    override fun toString(): String {
        return "AngleLine{" +
                "id=" + id +
                ", startPair=" + startPair +
                ", length=" + length +
                ", angle=" + angle +
                ", angleOffset=" + angleOffset +
                ", connectedTo=" + connectedTo +
                ", endPair=" + endPair +
                '}'
    }
}