package com.kjipo.representation.prototype

import com.kjipo.representation.raster.FlowDirection
import com.kjipo.representation.segment.Segment
import com.kjipo.representation.segment.SegmentImpl
import com.kjipo.segmentation.computeLine
import kotlin.math.sqrt
import kotlin.math.pow

open class LinePrototype(
    val startPair: com.kjipo.representation.segment.Pair,
    val endPair: com.kjipo.representation.segment.Pair
) :
    AdjustablePrototype {

    override fun getSegments(): List<Segment> {
        val newStartPair = Pair(startPair.row, startPair.column)
        val newEndPair = Pair(endPair.row, endPair.column)
        val linePairs = computeLine(newStartPair, newEndPair)
        return listOf(
            SegmentImpl(
                linePairs.map { kotlinPair: Pair<Int?, Int?> ->
                    com.kjipo.representation.segment.Pair(
                        kotlinPair.component1()!!, kotlinPair.component2()!!
                    )
                }.toList()
            )
        )
    }

    private fun moveStartPair(flowDirection: FlowDirection): LinePrototype {
        return LinePrototype(
            com.kjipo.representation.segment.Pair(
                startPair.row + flowDirection.rowShift,
                startPair.column + flowDirection.columnShift
            ),
            com.kjipo.representation.segment.Pair(endPair.row, endPair.column)
        )
    }

    private fun moveEndPair(flowDirection: FlowDirection): LinePrototype {
        return LinePrototype(
            com.kjipo.representation.segment.Pair(startPair.row, startPair.column),
            com.kjipo.representation.segment.Pair(
                endPair.row + flowDirection.rowShift,
                endPair.column + flowDirection.columnShift
            )
        )
    }

    override fun getMovements(): List<AdjustablePrototype> = FlowDirection.values()
        .flatMap { flowDirection ->
            listOf(
                moveStartPair(flowDirection),
                moveEndPair(flowDirection)
            )
        }
        .filter { linePrototype: LinePrototype -> linePrototype.startPair != linePrototype.endPair }

    val distance: Double
        get() = sqrt(
            (endPair.row - startPair.row).toDouble().pow(2)
                    + (endPair.column - startPair.column).toDouble().pow(2)
        )

    override fun toString(): String {
        return "LinePrototype{" +
                "startPair=" + startPair +
                ", endPair=" + endPair +
                '}'
    }
}