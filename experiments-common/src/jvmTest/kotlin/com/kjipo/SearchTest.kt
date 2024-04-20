package com.kjipo

import org.junit.Test
import mu.KotlinLogging
import com.kjipo.experiments2.LookupSample
import com.kjipo.experiments2.FindSimilarLines
import com.kjipo.datageneration.CreateSamples

class SearchTest {
    val numberOfRows = 64
    val numberOfColumns = 64


    @Test
    fun testSearch() {
        val logger = KotlinLogging.logger {}

        val inputSample = CreateSamples.generateSample(0, true, numberOfRows, numberOfColumns, 10)

        val lookupSamples = (1 until 10).map { id ->
            LookupSample(id, CreateSamples.generateSample(true, numberOfRows, numberOfColumns, 10))
        }.toList()

        val indicesInInputToUse = listOf(10, 12, 13, 11)
        val searchDescription = FindSimilarLines.findSimilarPaths(
            inputSample,
            indicesInInputToUse,
            lookupSamples
        )

        logger.info { searchDescription.similarSamples.listPathLengths() }

    }

}