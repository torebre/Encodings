package com.kjipo

import com.kjipo.experiments.LookupSample
import com.kjipo.experiments.SearchDescription
import kotlinx.browser.document
import mu.KotlinLogging

class SearchOverview(
    lookupSamples: List<LookupSample>,
    numberOfRows: Int,
    numberOfColumns: Int,
    searchDescription: SearchDescription,
    parentElement: String
) {
    private val searchVisualizations: List<SearchVisualization>
    private val logger = KotlinLogging.logger {}


    init {
        val element = document.getElementById(parentElement)
        searchVisualizations = lookupSamples.map { lookupSample ->
            logger.debug { "Adding visualization for sample with ID: ${lookupSample.id}" }
            val parentElementId = "sample-${lookupSample}"
            document.createElement("div").also {
                it.setAttribute("id", parentElementId)
                element!!.appendChild(it)
            }
            SearchVisualization(numberOfRows, numberOfColumns, lookupSample, searchDescription, parentElementId).also {
                // TODO
                it.showSearch(0)
            }
        }.toList()

    }

    fun showStep(searchStep: Int) {
        searchVisualizations.forEach { it.showSearch(searchStep) }
    }

    fun markLinesInSample(lookupSampleIndex: Int, lineIds: List<Int>, value: Int, colour: String) {
        searchVisualizations[lookupSampleIndex].markLines(lineIds, value, colour)
    }

}