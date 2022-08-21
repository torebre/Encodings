import com.kjipo.InputSampleVisualization
import com.kjipo.SearchOverview
import com.kjipo.SearchStepOverview
import com.kjipo.SearchVisualization
import com.kjipo.datageneration.CreateSamples
import com.kjipo.experiments.FindSimilarLines
import com.kjipo.experiments.LookupSample
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel
import mu.KotlinLogging


fun main() {
    KotlinLoggingConfiguration.LOG_LEVEL = KotlinLoggingLevel.DEBUG
    val logger = KotlinLogging.logger {}

    val numberOfRows = 64
    val numberOfColumns = 64
    val sample = CreateSamples.generateSample(true, numberOfRows, numberOfColumns, 10)

    val lookupSamples = (1 until 10).map { id ->
        LookupSample(id, CreateSamples.generateSample(true, numberOfRows, numberOfColumns, 10))
    }.toList()

    val searchDescription = FindSimilarLines.findSimilarPaths(sample, lookupSamples)

    val inputSampleVisualization =
        InputSampleVisualization(numberOfRows, numberOfColumns, "input_data", sample, searchDescription)
    inputSampleVisualization.markLines(listOf(10, 11, 12, 13), 3, "blue")

    logger.info { "Search description:" }

    searchDescription.similarSamples.listPathLengths().forEach {
        println("${it.first}, ${it.second.sampleId}")
    }

    val searchOverview =
        SearchOverview(lookupSamples, numberOfRows, numberOfColumns, searchDescription, "search_visualization")

    val searchStepOverview =
        SearchStepOverview(searchDescription, searchOverview, inputSampleVisualization, "search_steps_overview")

    for (i in lookupSamples.indices) {
        searchOverview.markLinesInSample(i, listOf(10, 11, 12, 13), 3, "blue")
    }

}