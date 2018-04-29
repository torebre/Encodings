package com.kjipo.experiments

import com.kjipo.prototype.CreatePrototypeDataset
import com.kjipo.prototype.Prototype
import com.kjipo.representation.EncodedKanji
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.createRectangleFromEncompassingPoints
import com.kjipo.segmentation.extractEmbeddedRegion
import com.kjipo.segmentation.zoomRegion
import com.kjipo.setup.transformKanjiData
import com.kjipo.visualization.displayKanjis
import com.kjipo.visualization.displayRasters
import com.kjipo.visualization.loadEncodedKanji
import com.kjipo.visualization.loadKanjisFromDirectory
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors
import kotlin.streams.toList


private val log = LoggerFactory.getLogger("RegionExtractionExperiments")


private val fittedPrototypes = Paths.get("/home/student/workspace/testEncodings/fittedPrototypes6")
private val encodedKanjiFolder = Paths.get("/home/student/workspace/testEncodings/kanji_output7")


fun extractRegionsAroundPrototypes() {
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
    if(!Files.exists(outputDirectory)) {
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


fun main(args: Array<String>) {
//    extractRegionsAroundPrototypes2()
    displayKanjis(Files.list(Paths.get("fragments"))
            .map {
                loadEncodedKanji(it,  { name -> name.substring(0, name.indexOf('_')).toInt() })
            }.toList())
}