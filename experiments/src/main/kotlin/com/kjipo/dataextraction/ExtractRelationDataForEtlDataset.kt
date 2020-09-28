package com.kjipo.dataextraction

import com.kjipo.experiments.ExtractRelationDataFromKanji
import com.kjipo.experiments.ReadEtlData
import com.kjipo.segmentation.fitMultipleLinesUsingDevianceMeasure
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.makeThin
import com.kjipo.utilities.DisplayUtilities
import java.nio.file.Files
import java.nio.file.Path


object ExtractRelationDataForEtlDataset {

    @ExperimentalStdlibApi
    private fun extractData(outputDirectory: Path) {
        // TODO Just use 10 for testing
        val etlKanjiData = ReadEtlData.getEtlKanjiData(10)

        if (!Files.exists(outputDirectory)) {
            Files.createDirectories(outputDirectory)
        }

        for (kanjiData in etlKanjiData) {
//            val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8")).filter { it.unicode == 33541 }.toList()
//            ExtractRelationDataFromKanji.extractRelationData({ fitMultipleLinesUsingDevianceMeasure(makeThin(shrinkImage(it, 64, 64))) }, 64, Paths.get("position_data_test.csv"), loadedKanji)

//            val kanjiToProcess = loadKanjisFromDirectory(Paths.get("kanji_output8")).filter { it.unicode == 33541 }.toList()

            Files.newBufferedWriter(outputDirectory.resolve("test_output.csv")).use { outputWriter ->
                outputWriter.write("unicode, relative_length, angle_diff, start_pair_distance, relative_distance, start_pair_angle_diff, id_line1, id_line2")
                outputWriter.newLine()

                etlKanjiData.stream().flatMap {
                    // TODO Use unicode and not kanji code
                    ExtractRelationDataFromKanji.transformToLineData({ fitMultipleLinesUsingDevianceMeasure(makeThin(shrinkImage(it, 64, 64))) }, it.kanjiData, 64, it.kanjiCode).stream()
                }
                        .forEach {
                            with(it) {
                                outputWriter.write("$unicode, $relativeLength, $angleDiff, $startPairDistance, $relativeStartDistance, $startPairAngleDiff, $lineFrom, $lineTo\n")
                            }
                        }

//                kanjiToProcess.stream().limit(50)
//                        .flatMap { ExtractRelationDataFromKanji.transformKanjiToRelationData(it, { fitMultipleLinesUsingDevianceMeasure(makeThin(shrinkImage(it, 64, 64))) }, 64).stream() }
//                        .forEach {
//                            with(it) {
//                                outputWriter.write("$unicode, $relativeLength, $angleDiff, $startPairDistance, $relativeStartDistance, $startPairAngleDiff, $lineFrom, $lineTo\n")
//                            }
//                        }
            }
        }


    }


    @ExperimentalStdlibApi
    private fun displayEtlTestImages() {
        val etlKanjiData = ReadEtlData.getEtlKanjiData(50)
        val kanjiLines = etlKanjiData.map { fitMultipleLinesUsingDevianceMeasure(makeThin(shrinkImage(it.kanjiData, 64, 64))) }.toList()
        val kanjiTexts = etlKanjiData.map { it.kanjiCode.toString() }.toList()

        DisplayUtilities.displayLinesUsingColourPalette(kanjiLines, kanjiTexts, etlKanjiData[0].kanjiData.numberOfRows, etlKanjiData[0].kanjiData.numberOfColumns)
    }


    @ExperimentalStdlibApi
    @JvmStatic
    fun main(args: Array<String>) {
//        extractData(Paths.get("etl_line_data"))
        displayEtlTestImages()
    }


}
