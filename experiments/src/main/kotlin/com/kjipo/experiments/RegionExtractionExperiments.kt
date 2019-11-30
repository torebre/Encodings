package com.kjipo.experiments

import com.kjipo.prototype.AngleLine
import com.kjipo.prototype.CreatePrototypeDataset
import com.kjipo.prototype.Prototype
import com.kjipo.raster.match.MatchDistance
import com.kjipo.representation.EncodedKanji
import com.kjipo.segmentation.*
import com.kjipo.setup.transformKanjiData
import com.kjipo.skeleton.makeThin
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToArrays
import com.kjipo.skeleton.transformToBooleanArrays
import com.kjipo.visualization.*
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.Collections.synchronizedMap
import java.util.stream.Collectors
import kotlin.collections.HashMap
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.roundToInt
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

    private fun extractSubImages(kanjiList: List<EncodedKanji>): List<SubImageHolder> {
        // TODO Using a subset now for quicker testing
        return kanjiList.subList(0, min(kanjiList.size, 1000)).parallelStream().flatMap { encodedKanji ->
            println("Checking kanji: ${encodedKanji.unicode}")

            val linePrototypes = fitLinesToKanji(encodedKanji, 10)

            val subImages = extractPrototypesOneSideOfLines(linePrototypes)
            var subImageId = 0
            subImages.stream().map { subImage ->
                val pointsInLine = subImage.stream().flatMap { it.segments.first().pairs.stream() }.map { Pair(it.row, it.column) }.toList()
                val rectangle = zoomRegion(createRectangleFromEncompassingPoints(pointsInLine), 64, 64)
                val distanceMatrix = transformArraysToMatrix(MatchDistance.computeDistanceMap(transformToBooleanArrays(rectangle)))

                SubImageHolder(encodedKanji, subImage, distanceMatrix, rectangle, ++subImageId)
            }

        }.toList()

    }

    private fun fitLinesToKanji(encodedKanji: EncodedKanji, minimumLengthToInclude: Int = 1): List<AngleLine> {
        val kanjiMatrix = transformArraysToMatrix(encodedKanji.image)
        val standardizedImage = makeThin(shrinkImage(kanjiMatrix, 64, 64))
        val linePrototypes = fitMultipleLinesUsingDevianceMeasure(standardizedImage).filter { it.length >= minimumLengthToInclude }
        return linePrototypes
    }


    private fun testExtraction(kanjiSubImages: List<SubImageHolder>, outputFile: Path) {
        println("Number of subimages: ${kanjiSubImages.size}")

        // TODO Using a sublist for testing
        val subList = kanjiSubImages.stream().limit(5000)
                .filter {
                    // Trying to include only sub images with some lines to see if interesting clusters with low distances are found
                    it.subImages.size > 5
                }
                .toList()

        writeKanjiDataSegments(subList, outputFile)

        val subImageDistances = synchronizedMap(HashMap<String, Int>())
        val seenComparisons = mutableSetOf<Pair<Set<Int>, Set<Int>>>()

        var counter = 0
        for (kanjiSubImage in subList) {
            println("Counter: ${counter++}")

            for (subImageHolder in subList) {
                // Only look for similar shapes in different kanji for now
                if (kanjiSubImage.encodedKanji.unicode == subImageHolder.encodedKanji.unicode) {
                    continue
                }

                val id1 = kanjiSubImage.id
                val id2 = subImageHolder.id

                val identifier = Pair(setOf(kanjiSubImage.encodedKanji.unicode, subImageHolder.encodedKanji.unicode), setOf(id1, id2))
                if (seenComparisons.contains(identifier)) {
                    continue
                }

                val idPart = "$id1-$id2"
                val key = kanjiSubImage.encodedKanji.unicode.toString() + "-" + subImageHolder.encodedKanji.unicode.toString() + "-" + idPart

                subImageDistances[key] = computeImageDistance(kanjiSubImage, subImageHolder)
                seenComparisons.add(identifier)
            }

        }

        Files.newBufferedWriter(Paths.get("sub_image_distances.csv")).use { bufferedWriter ->
            subImageDistances.entries.forEach {
                bufferedWriter.write(it.key)
                bufferedWriter.write(",")
                bufferedWriter.write(it.value.toString())
                bufferedWriter.newLine()
            }
        }

        displaySubImages(kanjiSubImages)
    }

    private fun displaySubImages(kanjiSubImages: List<SubImageHolder>) {
        val colourRasters = kanjiSubImages.stream().limit(800).map { subImageHolder ->
            val pointsInLine = subImageHolder.subImages.stream().flatMap { it.segments.first().pairs.stream() }.map { Pair(it.row, it.column) }.toList()
            val rectangle = zoomRegion(createRectangleFromEncompassingPoints(pointsInLine), 64, 64)

            val colourRaster = Array(rectangle.numberOfRows) { row ->
                Array(rectangle.numberOfColumns) { column ->
                    if (rectangle[row, column]) {
                        Color.WHITE
                    } else {
                        Color.BLACK
                    }
                }
            }
            colourRaster
        }.toList()

        displayColourRasters(colourRasters, squareSize = 1)
    }

    private fun writeKanjiDataSegments(kanjiSegments: Collection<SubImageHolder>, outputFile: Path) {
        Files.newBufferedWriter(outputFile).use { outputWriter ->
            outputWriter.write("unicode, line_number, angle, length, start_x, start_y, segment")
            outputWriter.newLine()

            kanjiSegments.forEach { subImageHolder ->
                for ((counter, subImage) in subImageHolder.subImages.withIndex()) {
                    val length = if (subImage.length.roundToInt() == 0) {
                        1.0
                    } else {
                        subImage.length
                    }

                    outputWriter.write(subImageHolder.encodedKanji.unicode.toString() + "," + counter
                            + "," + subImage.angle + "," + length
                            + "," + subImage.startPair.row + "," + subImage.startPair.column
                            + "," + subImageHolder.id)
                    outputWriter.newLine()
                }
            }
        }

    }

    private fun computeImageDistance(kanjiSubImage: SubImageHolder, kanjiSubImage2: SubImageHolder): Int {
        var distance1 = 0
        kanjiSubImage.pixelMatrix.forEachIndexed { row, column, value ->
            if (value) {
                distance1 += kanjiSubImage2.distanceMatrix[row, column]
            }
        }

        var distance2 = 0
        kanjiSubImage2.pixelMatrix.forEachIndexed { row, column, value ->
            if (value) {
                distance2 += kanjiSubImage.distanceMatrix[row, column]
            }
        }

        return ((distance1 + distance2) / 2) * (kanjiSubImage.subImages.size + kanjiSubImage2.subImages.size)
    }


    private data class SubImageHolder(val encodedKanji: EncodedKanji, val subImages: MutableList<AngleLine>, val distanceMatrix: Matrix<Int>, val pixelMatrix: Matrix<Boolean>, val id: Int)


    /**
     * Extract sub images that lie on one side of a line
     */
    private fun extractPrototypesOneSideOfLines(linePrototypes: List<AngleLine>): MutableList<MutableList<AngleLine>> {
        val lineSets = mutableListOf<MutableList<AngleLine>>()
        for (linePrototype in linePrototypes) {
            if (linePrototype.length < 2) {
                // Skip lines that are short
                continue
            }

            for (side in listOf(true, false)) {
                for (linesToInclude in 2 until 10) {
                    val lines = extractLines(linePrototype, linePrototypes, side, linesToInclude)
                    if (lines.isEmpty()) {
                        // No more lines found
                        break
                    }

                    val linesInNewSet = mutableListOf<AngleLine>()
                    linesInNewSet.add(linePrototype)
                    lines.map {
                        val rotatedStartPoint = rotateAboutPoint(it.startPair.column.toDouble(), it.startPair.row.toDouble(), linePrototype.startPair.column.toDouble(), linePrototype.startPair.row.toDouble(), -linePrototype.angle)
                        val rotatedEndPoint = rotateAboutPoint(it.endPair.column.toDouble(), it.endPair.row.toDouble(), linePrototype.startPair.column.toDouble(), linePrototype.startPair.row.toDouble(), -linePrototype.angle)

                        AngleLine(it.id, com.kjipo.raster.segment.Pair(rotatedStartPoint.second.roundToInt(), rotatedStartPoint.first.roundToInt()),
                                com.kjipo.raster.segment.Pair(rotatedEndPoint.second.roundToInt(), rotatedEndPoint.first.roundToInt()))
                    }.forEach {
                        linesInNewSet.add(it)
                    }

                    lineSets.add(linesInNewSet)
                }
            }
        }

        return lineSets
    }


    private fun rotateAboutPoint(x: Double, y: Double, rotationX: Double, rotationY: Double, angle: Double) =
            Pair(rotationX + (x - rotationX) * Math.cos(angle) - (y - rotationY) * Math.sin(angle),
                    rotationY + (x - rotationY) * Math.sin(angle) + (y - rotationY) * Math.cos(angle))


    private fun showRasters(numberOfRows: Int, numberOfColumns: Int, lineSets: MutableList<MutableList<AngleLine>>) {
        val colourRasters = mutableListOf<Array<Array<Color>>>()
        for (lineSet in lineSets) {
            val dispImage = Matrix(numberOfRows, numberOfColumns) { row, column ->
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
        displayColourRasters(colourRasters, squareSize = 1)

    }


    /**
     * Extracts lines that line on one side of the line given as the first input argument.
     * If the requested number of lines cannot be extracted, an empty list is returned.
     */
    private fun extractLines(linePrototype: AngleLine, linePrototypes: List<AngleLine>, side: Boolean, numberOfLinesToInclude: Int): MutableList<AngleLine> {
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

            val lengthStartPoint = (startx * (y2 - y1) - starty * (x2 - x1) + x2 * y1 - y2 * x1).absoluteValue / linePrototype.length
            val lengthEndPoint = (endx * (y2 - y1) - endy * (x2 - x1) + x2 * y1 + y2 * x1).absoluteValue / linePrototype.length

            if (d.sign == d2.sign) {
                // Start and end point of line is one same side of line given as input
                if (side && d.sign > 0 || !side && d.sign < 0) {
                    lineDistanceMap[secondLine] = min(lengthStartPoint, lengthEndPoint)
                    result.add(secondLine)
                }
            } else {
                // Line is crossing line given as input
                lineDistanceMap[secondLine] = min(lengthStartPoint, lengthEndPoint)
                result.add(secondLine)
            }
        }

        val cutOff = lineDistanceMap.values.sortedDescending().reversed().stream().skip(numberOfLinesToInclude.toLong()).findFirst().orElse(0.0)
        lineDistanceMap.entries.stream().filter {
            it.value < cutOff
        }
                .forEach { result.add(it.key) }

        if (result.size < numberOfLinesToInclude) {
            return mutableListOf()
        }
        return result
    }


    fun displayLinesInKanji(encodedKanji: EncodedKanji, linesInKanji: List<AngleLine>) {
        var maxRow = 0
        var maxColumn = 0

        for (angleLine in linesInKanji) {
            if (maxRow < angleLine.startPair.row) {
                maxRow = angleLine.startPair.row
            }
            if (maxRow < angleLine.endPair.row) {
                maxRow = angleLine.endPair.row
            }
            if (maxColumn < angleLine.startPair.column) {
                maxColumn = angleLine.startPair.column
            }
            if (maxColumn < angleLine.endPair.column) {
                maxColumn = angleLine.endPair.column
            }
        }
        val matrix = transformArraysToMatrix(encodedKanji.image)
        val dispImage = Matrix(maxRow, maxColumn, { row, column ->
            Color.BLACK
        })

        var counter = 0
        linesInKanji.forEach {
            it.segments.flatMap { it.pairs }.forEach {
                if (it.row >= 0 && it.row < dispImage.numberOfRows && it.column >= 0 && it.column < dispImage.numberOfColumns) {
                    dispImage[it.row, it.column] = Color.hsb(counter.toDouble().div(linesInKanji.size).times(360), 1.0, 1.0)
                }
            }
            ++counter
        }

        displayColourMatrix(dispImage, 5)
    }


    @JvmStatic
    fun main(args: Array<String>) {
//    extractRegionsAroundPrototypes2()
//        displayKanjis(Files.list(Paths.get("fragments"))
//                .map {
//                    loadEncodedKanji(it, { name -> name.substring(0, name.indexOf('_')).toInt() })
//                }.toList())


//        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8"))


//        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8"), mutableListOf(33760))
        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8"), 1000)

//    println("Loaded kanji: ${loadedKanji.size}")

//        displayKanjis(loadedKanji)

//        val linesFitToKanji = fitLinesToKanji(loadedKanji.first(), 10)

//        displayLinesInKanji(loadedKanji.first(), linesFitToKanji)

        val subImages = extractSubImages(loadedKanji)
//        displaySubImages(subImages)

        val outputFile = Paths.get("kanji_data_segments.csv")
        testExtraction(subImages, outputFile)
    }

}