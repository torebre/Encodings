package com.kjipo.experiments

import com.kjipo.datageneration.LinePrototypeWithAngle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min


class SimilarSamples() {

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


class SearchDescription(val similarSamples: SimilarSamples = SimilarSamples()) {
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


class LookupSample(val id: Int, val linePrototypes: List<LinePrototypeWithAngle>) {


}

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
//        angle_diff = abs(line1[0] - line2[0])

        val angleDiff = (line1.angle - line2.angle).let { diff ->
            if (diff > 2 * PI) {
                diff - 2 * PI
            } else if (diff < 0) {
                diff + 2 * PI
            } else {
                diff
            }
        }

//        if angle_diff > 2 * math.pi:
//        angle_diff -= 2 * math.pi
//        elif angle_diff < 0:
//        angle_diff += 2 * math.pi
//
//        midpoint_x_line1 = line1[1] if line1[1] < line1[3] else line1[3] + abs(line1[1] - line1[3]) / 2
//        midpoint_x_line2 = line2[1] if line2[1] < line2[3] else line2[3] + abs(line2[1] - line2[3]) / 2

        val midPointColumnLine1 =
            min(line1.startPair.column, line1.endPair.column) + abs(line1.startPair.column - line1.endPair.column) / 2.0
        val midPointColumnLine2 =
            min(line2.startPair.column, line2.endPair.column) + abs(line2.startPair.column - line2.endPair.column) / 2.0

        val midPointRowLine1 =
            min(line1.startPair.row, line1.endPair.row) + abs(line1.startPair.row - line1.endPair.row) / 2.0
        val midPointRowLine2 =
            min(line2.startPair.row, line2.endPair.row) + abs(line2.startPair.row - line2.endPair.row) / 2.0

//        midpoint_y_line1 = line1[2] if line1[2] < line1[4] else line1[4] + abs(line1[2] - line1[4]) / 2
//        midpoint_y_line2 = line2[3] if line2[2] < line2[4] else line2[4] + abs(line2[2] - line2[4]) / 2

        return LinePairDescription(
            angleDiff, midPointRowLine1, midPointColumnLine1,
            midPointRowLine2, midPointColumnLine2
        )

    }


    fun findSimilarPaths(
        prototypesWithAngles: List<LinePrototypeWithAngle>,
        lookupSamples: List<LookupSample>
    ): SearchDescription {
//        '''
//
//        :param test_sample:
//        :param indices_of_lines_to_use
//        :param data: An array where the columns are angle difference, midpoint x difference, midpoint y difference,
//        sample index, line 1 index within sample and line 2 index within sample
//        :return:
//        '''
//        current_index = indices_of_lines_to_use[0]
//        similar_samples: SimilarSamples = SimilarSamples(input_sample_id, test_sample, indices_of_lines_to_use)

        val searchDescription = SearchDescription()

        val linePairLookupData = lookupSamples.flatMap { lookupSample ->
            describeLinePairs(lookupSample.id, lookupSample.linePrototypes)
        }.toList()

        var stepId = 0
        for (index in 0 until prototypesWithAngles.size - 1) {

            searchDescription.addInputLines(stepId, index, index + 1)
            val lineRelation = describeLinePair(prototypesWithAngles[index], prototypesWithAngles[index + 1])
            val closestLines = findClosestLinesInData(lineRelation, linePairLookupData)

            searchDescription.extendPaths(closestLines.subList(0, 10), stepId)
            ++stepId
        }

//
//        similar_samples.input_sample_id = input_sample_id
//
//        search_description = SearchDescription()
//        search_description.test_sample = test_sample
//        search_description.similar_samples = similar_samples
//
//        for line_index in indices_of_lines_to_use[1:]:
//        search_step = SearchStep()
//        search_description.search_steps.append(search_step)
//
//        search_step.index_of_first_line = current_index
//        search_step.index_of_second_line = line_index
//
//        # Describe the relation between two lines
//                first_line_in_relation = test_sample[current_index]
//        second_line_in_relation = test_sample[line_index]
//
//        search_step.first_line_in_relation = first_line_in_relation
//        search_step.second_line_in_relation = second_line_in_relation
//
//        (angle_diff, midpoint_x_diff, midpoint_y_diff) = describe_two_lines(first_line_in_relation,
//        second_line_in_relation)
//        # Look for similar relations between lines in the data set
//                (row_indices_of_closest_lines_across_lookup_examples, first_distances) = find_closest_lines_in_data(angle_diff,
//        midpoint_x_diff,
//        midpoint_y_diff,
//        data,
//        number_of_closest_lines_to_return=10)
//        search_step.row_indices_of_closest_lines_across_lookup_examples = row_indices_of_closest_lines_across_lookup_examples
//        search_step.first_distances = first_distances
//
//        counter = 0
//        for index in row_indices_of_closest_lines_across_lookup_examples:
//        row = data[index]
//        # row[3] is the sample ID
//        # row[4] is the ID of the last path element
//        # row[5] is the ID of the new to-element
//        paths_to_extend = similar_samples.find_paths_where_last_step_is_matching(int(row[3]), int(row[4]),
//            int(row[5]))
//        search_step.paths_to_extend = paths_to_extend
//
//        if len(paths_to_extend) == 0:
//        if not similar_samples.exists_path_starting_with_id(int(row[3])):
//        similar_samples.start_new_path(int(row[3]), int(row[4]), int(row[5]), first_distances[counter])
//        else:
//        for path in paths_to_extend:
//        similar_samples.add_path_with_one_more_element(path, int(row[5]),
//            first_distances[counter])
//
//        counter += 1
//
//        current_index = line_index

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