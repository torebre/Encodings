package com.kjipo.experiments

import com.kjipo.prototype.CreatePrototypeDataset
import com.kjipo.prototype.Prototype
import com.kjipo.representation.EncodedKanji
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.createRectangleFromEncompassingPoints
import com.kjipo.segmentation.extractEmbeddedRegion
import com.kjipo.segmentation.zoomRegion
import com.kjipo.visualization.displayRasters
import com.kjipo.visualization.loadKanjisFromDirectory
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors


val log = LoggerFactory.getLogger("RegionExtractionExperiments")




fun extractRegionsAroundPrototypes() {
    val fittedPrototypes = Files.walk(Paths.get("/home/student/workspace/testEncodings/fittedPrototypes4"))
            .filter { path -> Files.isRegularFile(path) }
            .limit(10)
            .collect(Collectors.toMap<Path, Int, Prototype>({
                val fileName = it.fileName.toString()
                Integer.valueOf(fileName.substring(0, fileName.indexOf('.')))
            }) {
                Files.newInputStream(it).use {
                    CreatePrototypeDataset.readPrototype(it)
                }
            })

//        val encodedKanjis = FileInputStream(Parsers.FONT_FILE_LOCATION.toFile()).use({ fontStream ->
//            FontFileParser.parseFontFileUsingUnicodeInput(fittedPrototypes.keys, fontStream, 200, 200).stream()
//        }).collect(Collectors.toMap<EncodedKanji, Int, EncodedKanji>({
//            it.character.toInt()
//        }) {
//            it
//        })

    val encodedKanjis = loadKanjisFromDirectory(Paths.get("/home/student/workspace/testEncodings/kanji_output2")).stream()
            .collect(Collectors.toMap<EncodedKanji, Int, EncodedKanji>({
                it.unicode
            }) {
                it
            })

    val texts = mutableListOf<String>()
    val colourRasters = fittedPrototypes.entries.map {
        val currentKanji = encodedKanjis.getOrDefault(it.key, EncodedKanji(emptyArray(), 0))
        val image = currentKanji.image

        val matrix = Matrix(image.size, image[0].size, {row, column -> false})
        image.forEachIndexed({row, columnValues ->
            columnValues.forEachIndexed({column, value ->
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
    val fittedPrototypes = Files.walk(Paths.get("/home/student/workspace/testEncodings/fittedPrototypes4"))
            .filter { path -> Files.isRegularFile(path) }
            .limit(50)
            .collect(Collectors.toMap<Path, Int, Prototype>({
                val fileName = it.fileName.toString()
                Integer.valueOf(fileName.substring(0, fileName.indexOf('.')))
            }) {
                Files.newInputStream(it).use {
                    CreatePrototypeDataset.readPrototype(it)
                }
            })

//        val encodedKanjis = FileInputStream(Parsers.FONT_FILE_LOCATION.toFile()).use({ fontStream ->
//            FontFileParser.parseFontFileUsingUnicodeInput(fittedPrototypes.keys, fontStream, 200, 200).stream()
//        }).collect(Collectors.toMap<EncodedKanji, Int, EncodedKanji>({
//            it.character.toInt()
//        }) {
//            it
//        })

    val encodedKanjis = loadKanjisFromDirectory(Paths.get("/home/student/workspace/testEncodings/kanji_output2")).stream()
            .collect(Collectors.toMap<EncodedKanji, Int, EncodedKanji>({
                it.unicode
            }) {
                it
            })

    val texts = mutableListOf<String>()
    val colourRasters = fittedPrototypes.entries.flatMap {
        val currentKanji = encodedKanjis.getOrDefault(it.key, EncodedKanji(emptyArray(), 0))
        val image = currentKanji.image

        val kanjiCharacters = mutableListOf<String>()
        val matrix = Matrix(image.size, image[0].size, { row, column -> false })
        image.forEachIndexed({ row, columnValues ->
            columnValues.forEachIndexed({ column, value ->
                matrix[row, column] = value
            })
        })
        kanjiCharacters.add(currentKanji.unicode.toString().plus(": ").plus(String(Character.toChars(currentKanji.unicode))))

        if (image.isEmpty()) {
            log.error("Image is empty")
        }

        val embeddedRegions = it.value.segments.map {
            extractEmbeddedRegion(matrix, it.pairs, 90.0, it.pairs.size.toDouble())
        }.toList()

        var counter = 0

        val rasters = embeddedRegions.map {
            val rectangle = zoomRegion(createRectangleFromEncompassingPoints(it), 100, 100)

            val colourRaster = Array(rectangle.numberOfRows, { row ->
                Array(rectangle.numberOfColumns, { column ->
                    if (rectangle[row, column]) {
                        Color.WHITE
                    } else {
                        Color.BLACK
                    }
                })
            })
            texts.add(kanjiCharacters[counter])
            colourRaster
        }
                .toList()


        ++counter

        return@flatMap rasters

    }.toList()


    displayRasters(colourRasters, texts)


    Thread.sleep(Long.MAX_VALUE)

}


fun main(args: Array<String>) {
    extractRegionsAroundPrototypes()
}