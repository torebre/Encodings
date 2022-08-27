import com.kjipo.*
import com.kjipo.datageneration.CreateSamples
import com.kjipo.experiments.FindSimilarLines
import com.kjipo.experiments.LookupSample
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel
import mu.KotlinLogging


fun main() {
    KotlinLoggingConfiguration.LOG_LEVEL = KotlinLoggingLevel.DEBUG
    val logger = KotlinLogging.logger {}


    val inputSample = CreateSamples.generateSample(0, true, numberOfRows, numberOfColumns, 10)

    val lookupSamples = (1 until 10).map { id ->
        LookupSample(id, CreateSamples.generateSample(true, numberOfRows, numberOfColumns, 10))
    }.toList()

    val indicesInInputToUse = listOf(10, 11, 12, 13)
    val searchDescription = FindSimilarLines.findSimilarPaths(
        inputSample,
        indicesInInputToUse,
        lookupSamples
    )

    val inputSampleVisualization =
        InputSampleVisualization(numberOfRows, numberOfColumns, "input_data", inputSample)
    inputSampleVisualization.markLines(indicesInInputToUse, 3, "blue")

    val searchOverview =
        SearchOverview(lookupSamples, numberOfRows, numberOfColumns, searchDescription, "search_visualization")

    val pathOverview = PathOverview(searchDescription, "paths_overview", searchOverview, lookupSamples)

    val searchStepOverview =
        SearchStepOverview(searchDescription, searchOverview, inputSampleVisualization, "search_steps_overview")

    for (i in lookupSamples.indices) {
        searchOverview.markLinesInSample(i, listOf(10, 11, 12, 13), 3, "blue")
    }

}