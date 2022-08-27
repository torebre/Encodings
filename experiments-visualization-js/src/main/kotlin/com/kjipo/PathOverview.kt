package com.kjipo

import com.kjipo.experiments.LookupSample
import com.kjipo.experiments.SearchDescription
import com.kjipo.experiments.SearchPath
import createAndAddButton
import getColour
import kotlinx.browser.document
import mu.KotlinLogging

class PathOverview(
    private val searchDescription: SearchDescription, parentElementId: String, searchOverview: SearchOverview,
    private val lookupSamples: List<LookupSample>
) {

    private val logger = KotlinLogging.logger {}


    init {
        val element = document.getElementById(parentElementId)

        searchDescription.similarSamples.searchPaths.forEach { searchPath ->
            searchPath.value.mapIndexed { pathNumber, pathsForSample ->
                val linePairs = pathsForSample.path.map { linePairs ->
                    "${linePairs.line1Index} - ${linePairs.line2Index}"
                }
                val pathDescription =
                    "Sample ID: ${searchPath.key}. Path number: ${pathNumber}. Line pairs: $linePairs"

                logger.info { pathDescription }
            }

        }

        searchDescription.similarSamples.searchPaths.flatMap { it.value }.forEachIndexed { index, searchPath ->
            val showPathButton = createAndAddButton("path-${searchPath.sampleId}", "Path ${searchPath.sampleId}") {
                searchOverview.markLinesInSample(
                    searchPath.sampleId,
                    searchPath.path.flatMap { listOf(it.line1Index, it.line2Index) }.toSet(),
                    3,
                    getColour(3)
                )
            }
            element!!.appendChild(showPathButton)

            val pathParentElementId = "path-${index}-steps"
            document.createElement("div").also {
                it.setAttribute("id", pathParentElementId)
                element.appendChild(it)
            }
            showPath(searchPath, pathParentElementId)
        }
    }

    private fun showPath(searchPath: SearchPath, parentElementId: String) {
        val lookupSample = lookupSamples[searchPath.sampleId]
        searchPath.path.forEach { linePair ->
            val inputSampleVisualization =
                InputSampleVisualization(numberOfRows, numberOfColumns, parentElementId, lookupSample)
            inputSampleVisualization.markLines(listOf(linePair.line1Index, linePair.line2Index), 4, getColour(4))
        }
    }

    fun showStep(stepId: Int, inputSampleVisualization: InputSampleVisualization) {
        inputSampleVisualization.showHighlightLinesInCollection(searchDescription.nextInput.first { it.stepId == stepId }
            .let { listOf(it.line1Id, it.line2Id) })
    }

}