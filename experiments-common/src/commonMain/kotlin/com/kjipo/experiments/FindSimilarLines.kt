package com.kjipo.experiments

import com.kjipo.datageneration.LinePrototypeWithAngle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min


class SimilarSamples {

    val searchPaths = mutableMapOf<Int, MutableList<SearchPath>>()


    fun getPathsEndingWithLineId(sampleId: Int, lineId: Int): List<LinePair> {
        val paths = searchPaths[sampleId]?.mapNotNull { searchPath ->
            getLastLinePairIfMatch(searchPath, sampleId, lineId)
        }
        return paths ?: emptyList()
    }

    fun getLastLinePairIfMatch(searchPath: SearchPath, sampleId: Int, lineId: Int): LinePair? {
        return searchPath.path.last().let {
            if (it.sampleId == sampleId && it.line2Index == lineId) {
                it
            } else {
                null
            }
        }
    }

    fun listPathLengths(): List<Pair<Int, SearchPath>> {
        return searchPaths.values.flatten().map {
            Pair(it.path.size, it)
        }.toList().sortedBy { it.first }
    }


}

class SearchPath(val sampleId: Int, val path: MutableList<LinePair> = mutableListOf())

class SearchPlaythroughStep(val stepId: Int, val sampleId: Int, val lineAddedId: Int)

class InputLinePair(val stepId: Int, val line1Id: Int, val line2Id: Int)


class SearchDescription(val inputSample: LookupSample, val similarSamples: SimilarSamples = SimilarSamples()) {
    val searchPlayThrough = mutableListOf<SearchPlaythroughStep>()
    val nextInput = mutableListOf<InputLinePair>()

    fun extendPaths(linePairs: List<LinePair>, stepId: Int) {
        linePairs.forEach { linePair ->
            val existingPathsForSample = similarSamples.searchPaths[linePair.sampleId]

            if (existingPathsForSample == null) {
                similarSamples.searchPaths[linePair.sampleId] =
                    mutableListOf(SearchPath(linePair.sampleId, mutableListOf(linePair)))
                searchPlayThrough.add(SearchPlaythroughStep(stepId, linePair.sampleId, linePair.line1Index))
                searchPlayThrough.add(SearchPlaythroughStep(stepId, linePair.sampleId, linePair.line2Index))
            } else {
                existingPathsForSample.forEach { searchPath ->
                    similarSamples.getLastLinePairIfMatch(searchPath, linePair.sampleId, linePair.line2Index)?.let {
                        searchPath.path.add(linePair)
                        searchPlayThrough.add(SearchPlaythroughStep(stepId, linePair.sampleId, linePair.line2Index))
                    }
                }
            }
        }
    }

    fun addInputLines(stepId: Int, line1Id: Int, line2Id: Int) {
        nextInput.add(InputLinePair(stepId, line1Id, line2Id))
    }

}


class LookupSample(val id: Int, val linePrototypes: List<LinePrototypeWithAngle>)

data class LinePair(
    val sampleId: Int,
    val line1Index: Int,
    val line2Index: Int,
    val linePairDescription: LinePairDescription
)

data class LinePairDescription(
    val angleDiff: Double, val midPointRowLine1: Double, val midPointColumnLine1: Double,
    val midPointRowLine2: Double, val midPointColumnLine2: Double
)


object FindSimilarLines {

    fun describeLinePair(line1: LinePrototypeWithAngle, line2: LinePrototypeWithAngle): LinePairDescription {
        val angleDiff = (line1.angle - line2.angle).let { diff ->
            if (diff > 2 * PI) {
                diff - 2 * PI
            } else if (diff < 0) {
                diff + 2 * PI
            } else {
                diff
            }
        }

        val midPointColumnLine1 =
            min(line1.startPair.column, line1.endPair.column) + abs(line1.startPair.column - line1.endPair.column) / 2.0
        val midPointColumnLine2 =
            min(line2.startPair.column, line2.endPair.column) + abs(line2.startPair.column - line2.endPair.column) / 2.0

        val midPointRowLine1 =
            min(line1.startPair.row, line1.endPair.row) + abs(line1.startPair.row - line1.endPair.row) / 2.0
        val midPointRowLine2 =
            min(line2.startPair.row, line2.endPair.row) + abs(line2.startPair.row - line2.endPair.row) / 2.0

        return LinePairDescription(
            angleDiff, midPointRowLine1, midPointColumnLine1,
            midPointRowLine2, midPointColumnLine2
        )

    }


    fun findSimilarPaths(
        inputSample: LookupSample,
        indicesInInputToUse: Collection<Int>,
        lookupSamples: List<LookupSample>
    ): SearchDescription {
        val searchDescription = SearchDescription(inputSample)

        val linePairLookupData = lookupSamples.flatMap { lookupSample ->
            describeLinePairs(lookupSample.id, lookupSample.linePrototypes)
        }.toList()

        var stepId = 0
        for (index in 0 until inputSample.linePrototypes.size - 1) {
            if (!indicesInInputToUse.contains(index)) {
                continue
            }

            searchDescription.addInputLines(stepId, index, index + 1)
            val lineRelation =
                describeLinePair(inputSample.linePrototypes[index], inputSample.linePrototypes[index + 1])
            val closestLines = findClosestLinesInData(lineRelation, linePairLookupData)

            searchDescription.extendPaths(closestLines.subList(0, 10), stepId)
            ++stepId
        }

        return searchDescription
    }


    fun describeLinePairs(sampleId: Int, lineSample: List<LinePrototypeWithAngle>): MutableList<LinePair> {
        val linePairs = mutableListOf<LinePair>()
        for ((index, line) in lineSample.withIndex()) {
            for ((index2, line2) in lineSample.withIndex()) {
                linePairs.add(LinePair(sampleId, index, index2, describeLinePair(line, line2)))
            }
        }
        return linePairs
    }


    private fun findClosestLinesInData(
        linePairDescription: LinePairDescription,
        linePairs: List<LinePair>
    ): List<LinePair> {
//        '''
//        Returns a tuple where the first array contains the indices of the closest lines, and the
//        second array contains the distances.
//        '''
//        # TODO Using midpoint diff will not work for finding rectangles
//                angle_diffs = abs(angle_diff - _data[:, 0])
//        sorted_angle_diffs_indices = angle_diffs.argsort()
//
//        # Return the indices of the smallest angle differences
//                return (sorted_angle_diffs_indices[0:number_of_closest_lines_to_return],
//        angle_diffs[sorted_angle_diffs_indices[0:number_of_closest_lines_to_return]])

        val linePairsSortedByDistance = linePairs.sortedBy { linePair ->
            val angleDiff = abs(linePairDescription.angleDiff - linePair.linePairDescription.angleDiff)

            angleDiff
        }

        return linePairsSortedByDistance
    }


}