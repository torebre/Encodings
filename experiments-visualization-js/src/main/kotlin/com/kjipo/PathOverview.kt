package com.kjipo

import com.kjipo.experiments.SearchDescription
import createAndAddButton
import getColour
import kotlinx.browser.document
import mu.KotlinLogging

class PathOverview(searchDescription: SearchDescription, parentElementId: String, searchOverview: SearchOverview) {

    private val logger = KotlinLogging.logger {}


    init {
        val element = document.getElementById(parentElementId)

        searchDescription.similarSamples.searchPaths.forEach { searchPath ->
            searchPath.value.mapIndexed { pathNumber, pathsForSample ->
                val linePairs = pathsForSample.path.map { linePairs ->
                    "${linePairs.line1Index} - ${linePairs.line2Index}"
                }
                val pathDescription =
                    "Sample ID: ${searchPath.key}. Path number: ${pathNumber}. Line pairs: ${linePairs}"

                logger.info { pathDescription }
            }

        }

        searchDescription.similarSamples.searchPaths.flatMap { it.value }.forEach { searchPath ->
            val showPathButton = createAndAddButton("path-${searchPath.sampleId}", "Path ${searchPath.sampleId}") {
                searchOverview.markLinesInSample(
                    searchPath.sampleId,
                    searchPath.path.flatMap { listOf(it.line1Index, it.line2Index) }.toSet(),
                    3,
                    getColour(3)
                )
            }
            element!!.appendChild(showPathButton)
        }

        logger.info { "Search description:" }

        searchDescription.similarSamples.listPathLengths().forEach {
            println("${it.first}, ${it.second.sampleId}")
        }

    }

}