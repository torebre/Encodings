package com.kjipo

import com.kjipo.datageneration.CreateSamples
import com.kjipo.experiments.FindSimilarLines.describeLinePairs
import com.kjipo.experiments.FindSimilarLines.findSimilarPaths
import com.kjipo.experiments.LookupSample

object RunExperiment {


    @JvmStatic
    fun main(args: Array<String>) {
        val sample = CreateSamples.generateSample(true, 64, 64, 10)
        val description = describeLinePairs(0, sample)

        val lookupSamples = (1 until 10).map { id ->
            LookupSample(id, CreateSamples.generateSample(true, 64, 64, 10))
        }.toList()

        val searchDescription = findSimilarPaths(sample, lookupSamples)

        println("Search description:")

        searchDescription.similarSamples.listPathLengths().forEach {
            println("${it.first}, ${it.second.sampleId}")
        }

        val sample2Search = searchDescription.searchPlayThrough.filter {
            it.sampleId == 5
        }.toList()

        println("Search for sample 5:")
        sample2Search.forEach {
            println("${it.sampleId}, ${it.lineAddedId}")
        }


    }


}