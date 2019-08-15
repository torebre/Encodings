package com.kjipo.experiments

import com.kjipo.prototype.AngleLine
import com.kjipo.prototype.CreatePrototypeDataset
import com.kjipo.prototype.Prototype
import com.kjipo.representation.EncodedKanji
import com.kjipo.segmentation.*
import com.kjipo.setup.transformKanjiData
import com.kjipo.skeleton.makeThin
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToArrays
import com.kjipo.visualization.*
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.sign
import kotlin.streams.toList


object RegionExtractionExperiments {
    private val fittedPrototypes = Paths.get("/home/student/workspace/testEncodings/fittedPrototypes6")
    private val encodedKanjiFolder = Paths.get("/home/student/workspace/testEncodings/kanji_output7")

    private val log = LoggerFactory.getLogger(RegionExtractionExperiments.javaClass)


    private fun extractRegionsAroundPrototypes() {
        val numberOfKanjiToInclude = 10L

        val fittedPrototypes = Files.walk(fittedPrototypes)
                .filter { path -> Files.isRegularFile(path) }
                .limit(numberOfKanjiToInclude)
                .collect(Collectors.toMap<Path, Int, Prototype>({
                    val fileName = it.fileName.toString()
                    Integer.valueOf(fileName.substring(0, fileName.indexOf('.')))
                }) {
                    Files.newInputStream(it).use {
                        CreatePrototypeDataset.readPrototype(it)
                    }
                })

        val encodedKanjis = loadKanjisFromDirectory(encodedKanjiFolder).stream()
                .collect(Collectors.toMap<EncodedKanji, Int, EncodedKanji>({
                    it.unicode
                }) {
                    it
                })

        val texts = mutableListOf<String>()
        val colourRasters = fittedPrototypes.entries.map {
            val currentKanji = encodedKanjis.getOrDefault(it.key, EncodedKanji(emptyArray(), 0))
            val image = currentKanji.image

            val matrix = Matrix(image.size, image[0].size, { row, column -> false })
            image.forEachIndexed({ row, columnValues ->
                columnValues.forEachIndexed({ column, value ->
                    matrix[row, column] = value
                })
            })
            texts.add(currentKanji.unicode.toString().plus(": ").plus(String(Character.toChars(currentKanji.unicode))))

            if (image.isEmpty()) {
                log.error("Image is empty")
            }

            val embeddedRegions = it.value.segments.map {
                extractEmbeddedRegion(matrix, it.pairs, 90.0, it.pairs.size.toDouble())
            }.toList()


            val colourRaster = Array(image.size, { row ->
                Array(image[0].size, { column ->
                    if (image[row][column]) {
                        Color.WHITE
                    } else {
                        Color.BLACK
                    }
                })
            })

            embeddedRegions.forEach {
                it.forEach {
                    colourRaster[it.first][it.second] = Color.BLUE
                }
            }

            it.value.segments.forEach {
                it.pairs.forEach {
                    if (it.row < 0
                            || it.row >= colourRaster.size
                            || it.column < 0
                            || it.column >= colourRaster[0].size) {
                        log.error("Prototype outside allowed range")
                    } else {
                        colourRaster[it.row][it.column] = Color.RED
                    }

                }
            }

            colourRaster
        }.toList()


//        displayKanjis(encodedKanjis.values)
        displayRasters(colourRasters, texts)

        Thread.sleep(Long.MAX_VALUE)

    }


    fun extractRegionsAroundPrototypes2() {
        val outputDirectory = Paths.get("fragments")
        if (!Files.exists(outputDirectory)) {
            Files.createDirectory(outputDirectory)
        }
        val numberOfKanjiToInclude = 50L
        val finalNumberOfRows = 100
        val finalNumberOfColumns = 100

        val fittedPrototypes = Files.walk(fittedPrototypes)
                .filter { path -> Files.isRegularFile(path) }
                .limit(numberOfKanjiToInclude)
                .collect(Collectors.toMap<Path, Int, Prototype>({
                    val fileName = it.fileName.toString()
                    Integer.valueOf(fileName.substring(0, fileName.indexOf('.')))
                }) {
                    Files.newInputStream(it).use {
                        CreatePrototypeDataset.readPrototype(it)
                    }
                })

        val encodedKanjis = loadKanjisFromDirectory(encodedKanjiFolder).stream()
                .collect(Collectors.toMap<EncodedKanji, Int, EncodedKanji>({
                    it.unicode
                }) {
                    it
                })

        val texts = mutableListOf<String>()

        val colourRasters = fittedPrototypes.entries.flatMap {
            val currentKanji = encodedKanjis.getOrDefault(it.key, EncodedKanji(emptyArray(), 0))
            val image = currentKanji.image

            val kanjiCharacters = currentKanji.unicode.toString().plus(": ").plus(String(Character.toChars(currentKanji.unicode)))
            val matrix = Matrix(image.size, image[0].size, { row, column -> false })
            image.forEachIndexed({ row, columnValues ->
                columnValues.forEachIndexed({ column, value ->
                    matrix[row, column] = value
                })
            })

            if (image.isEmpty()) {
                log.error("Image is empty")
            }

            val embeddedRegions = it.value.segments.map {
                extractEmbeddedRegion(matrix, it.pairs, 90.0, it.pairs.size.toDouble())
            }.toList()

            var embeddedRegionCounter = 0
            val rasters = embeddedRegions.map {
                val rectangle = zoomRegion(createRectangleFromEncompassingPoints(it), finalNumberOfRows, finalNumberOfColumns)

                val colourRaster = Array(rectangle.numberOfRows, { row ->
                    Array(rectangle.numberOfColumns, { column ->
                        if (rectangle[row, column]) {
                            Color.WHITE
                        } else {
                            Color.BLACK
                        }
                    })
                })
                texts.add(kanjiCharacters)

                Files.newBufferedWriter(outputDirectory.resolve(currentKanji.unicode.toString().plus("_").plus(embeddedRegionCounter).plus(".dat")),
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE).use {
                    it.write(transformKanjiData(rectangle, finalNumberOfRows, finalNumberOfColumns))
                }

                ++embeddedRegionCounter

                colourRaster
            }
                    .toList()


            return@flatMap rasters

        }.toList()


        displayRasters(colourRasters, texts)


        Thread.sleep(Long.MAX_VALUE)

    }


    private fun testExtraction() {
        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8"), 1)

        for (encodedKanji in loadedKanji) {
            val kanjiMatrix = transformArraysToMatrix(encodedKanji.image)
            val standardizedImage = makeThin(shrinkImage(kanjiMatrix, 64, 64))
            val linePrototypes = fitMultipleLinesUsingDevianceMeasure(standardizedImage)

            extractRegionsAroundPrototypes3(standardizedImage, linePrototypes)
        }

    }


    private fun extractRegionsAroundPrototypes3(image: Matrix<Boolean>, linePrototypes: List<AngleLine>) {
        val lineSets = mutableListOf<MutableList<AngleLine>>()
        for (linePrototype in linePrototypes) {
            if (linePrototype.length < 2) {
                continue
            }
            lineSets.add(extractLines(linePrototype, linePrototypes, true))
            lineSets.add(extractLines(linePrototype, linePrototypes, false))
        }

        val colourRasters = mutableListOf<Array<Array<Color>>>()
        for (lineSet in lineSets) {
            val dispImage = Matrix(image.numberOfRows, image.numberOfColumns) { row, column ->
                Color.BLACK
            }

            lineSet.forEach {
                it.segments.flatMap { it.pairs }.forEach {
                    if (it.row >= 0 && it.row < dispImage.numberOfRows && it.column >= 0 && it.column < dispImage.numberOfColumns) {
                        dispImage[it.row, it.column] = Color.WHITE
                    }
                }
            }
            colourRasters.add(transformToArrays(dispImage))
        }

        // TODO Only looking at sublist while debugging
        displayColourRasters(colourRasters.subList(0, 10), squareSize = 5)
    }


    private fun extractLines(linePrototype: AngleLine, linePrototypes: List<AngleLine>, side: Boolean): MutableList<AngleLine> {
        val x1 = linePrototype.startPair.column.toDouble()
        val y1 = linePrototype.startPair.row.toDouble()

        val x2 = linePrototype.endPair.column.toDouble()
        val y2 = linePrototype.endPair.row.toDouble()

        val result = mutableListOf<AngleLine>()


        val lineDistanceMap = mutableMapOf<AngleLine, Double>()

        for (secondLine in linePrototypes) {
            if (linePrototype == secondLine
                    || secondLine.length < 2) {
                continue
            }

            val startx = secondLine.startPair.column
            val endx = secondLine.endPair.column
            val starty = secondLine.startPair.row
            val endy = secondLine.endPair.row

            val d = (startx - x1) * (y2 - y1) - (starty - y1) * (x2 - x1)
            val d2 = (endx - x1) * (y2 - y1) - (endy - y1) * (x2 - x1)

//            if (d.absoluteValue >= 0.01
//                    && d2.absoluteValue >= 0.01
//                    && d.sign == d2.sign) {
//                continue
//            }

            val lengthStartPoint = (startx * (y2 - y1) - starty * (x2 - x1) + x2 * y1 - y2 * x1).absoluteValue / linePrototype.length
            val lengthEndPoint = (endx * (y2 - y1) - endy * (x2 - x1) + x2 * y1 + y2 * x1).absoluteValue / linePrototype.length

            if (d.sign == d2.sign) {
                if (side && d.sign > 0) {
                    lineDistanceMap[secondLine] = min(lengthStartPoint, lengthEndPoint)
                } else if (!side && d.sign < 0) {
                    lineDistanceMap[secondLine] = min(lengthStartPoint, lengthEndPoint)
                }
            } else {
                result.add(secondLine)
            }
        }

        val cutOff = lineDistanceMap.values.sortedDescending().reversed().stream().skip(5).findFirst().orElse(0.0)
        lineDistanceMap.entries.stream().filter {
            it.value <= cutOff
        }
                .forEach { result.add(it.key) }

        return result
    }


    @JvmStatic
    fun main(args: Array<String>) {
//    extractRegionsAroundPrototypes2()
//        displayKanjis(Files.list(Paths.get("fragments"))
//                .map {
//                    loadEncodedKanji(it, { name -> name.substring(0, name.indexOf('_')).toInt() })
//                }.toList())


        testExtraction()
    }

}