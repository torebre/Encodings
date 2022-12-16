package com.kjipo.experiments

import com.kjipo.datageneration.LinePrototypeWithAngle
import mu.KotlinLogging
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min


private val logger = KotlinLogging.logger {}

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

class SearchPath(val sampleId: Int, val path: MutableList<LinePair> = mutableListOf()) {
    override fun toString(): String {
        return "SearchPath(sampleId=$sampleId, path=$path)"
    }
}

class SearchPlaythroughStep(val stepId: Int, val sampleId: Int, val lineAddedId: Int)

class InputLinePair(val stepId: Int, val line1Id: Int, val line2Id: Int)


class SearchDescription(
    val inputSample: LookupSample,
    val similarSamples: SimilarSamples = SimilarSamples()
) {
    val searchPlayThrough = mutableListOf<SearchPlaythroughStep>()
    val nextInput = mutableListOf<InputLinePair>()

    fun extendPaths(linePairs: List<LinePair>, stepId: Int) {
        linePairs.forEach { newLinePair ->
            val existingPathsForSample = similarSamples.searchPaths[newLinePair.sampleId]

            if (existingPathsForSample == null) {

                logger.info { "Test50. New search path for sample: ${newLinePair.sampleId}" }

                similarSamples.searchPaths[newLinePair.sampleId] =
                    mutableListOf(SearchPath(newLinePair.sampleId, mutableListOf(newLinePair)))
                searchPlayThrough.add(SearchPlaythroughStep(stepId, newLinePair.sampleId, newLinePair.line1Index))
                searchPlayThrough.add(SearchPlaythroughStep(stepId, newLinePair.sampleId, newLinePair.line2Index))
            } else {
                existingPathsForSample.forEach { searchPath ->

                    logger.info { "Test52. Found search paths for sample: ${newLinePair.sampleId}" }

                    val newPoints = setOf(newLinePair.line1Index, newLinePair.line2Index)
                    similarSamples.getLastLinePairIfMatch(searchPath, newLinePair.sampleId, newLinePair.line1Index)?.let {
                        if(searchPath.path.find { setOf(it.line1Index, it.line2Index) == newPoints } == null) {
                            logger.info { "Test51. Search path extended for sample: ${newLinePair.sampleId}. Line pair: ${it.line1Index}, ${it.line2Index}. New line pair: ${newLinePair.line1Index}, ${newLinePair.line2Index}" }
                            searchPath.path.add(newLinePair)
                            searchPlayThrough.add(SearchPlaythroughStep(stepId, newLinePair.sampleId, newLinePair.line2Index))
                        }
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
) {
    override fun toString(): String {
        return "LinePair(sampleId=$sampleId, line1Index=$line1Index, line2Index=$line2Index)"
    }
}

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
        indicesInInputToUse: List<Int>,
        lookupSamples: List<LookupSample>
    ): SearchDescription {
        val searchDescription = SearchDescription(inputSample)

        val linePairLookupData = lookupSamples.flatMap { lookupSample ->
            describeLinePairs(lookupSample.id, lookupSample.linePrototypes)
        }.toList()

        var stepId = 0

        indicesInInputToUse.forEachIndexed { index, value ->
            if (index != indicesInInputToUse.size - 1) {
                logger.info {
                    "Step ID: $stepId"
                }
                val nextValue = indicesInInputToUse[index + 1]
                searchDescription.addInputLines(stepId, value, nextValue)
                val lineRelation =
                    describeLinePair(inputSample.linePrototypes[value], inputSample.linePrototypes[nextValue])
                val closestLinesBySample =
                    findClosestLinesInData(lineRelation, linePairLookupData).groupBy { it.sampleId }

                val linesToUseForExtending = closestLinesBySample.map {
                    it.value.sortedBy { linePair ->
                        val angleDiff = abs(lineRelation.angleDiff - linePair.linePairDescription.angleDiff)
                        angleDiff
                    }.subList(0, 2)
                }.flatten()

//                searchDescription.extendPaths(closestLines.subList(0, 10), stepId)
                searchDescription.extendPaths(linesToUseForExtending, stepId)
                ++stepId
            }
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