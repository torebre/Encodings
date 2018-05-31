package com.kjipo.visualization

import com.kjipo.representation.EncodedKanji
import com.kjipo.segmentation.Matrix
import javafx.application.Application
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import tornadofx.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList


class RasterOverviewApplication : App() {
    override val primaryView = KanjiView::class
}

val log = LoggerFactory.getLogger(RasterOverviewApplication::class.java)


fun displayKanjis(encodedKanjis: Collection<EncodedKanji>, squareSize: Int = 1) {
    val startThread = Thread {
        Application.launch(RasterOverviewApplication::class.java)
    }
    startThread.start()

    val characters = mutableListOf<String>()
    val colourRasters = encodedKanjis.map {
        characters.add(String(Character.toChars(it.unicode)))

        Array(it.image.size, { row ->
            Array(it.image[0].size, { column ->
                if (it.image[row][column]) {
                    Color.WHITE
                } else {
                    Color.BLACK
                }
            })
        })
    }

    val kanjiView = FX.find(KanjiView::class.java)
    FX.runAndWait { kanjiView.loadRasters(colourRasters, characters, squareSize) }
}


fun displayColourRasters(colourRasters: Collection<Array<Array<Color>>>, texts: List<String> = emptyList(), squareSize: Int = 1) {
    val startThread = Thread {
        Application.launch(RasterOverviewApplication::class.java)
    }
    startThread.start()

    val kanjiView = FX.find(KanjiView::class.java)
    FX.runAndWait { kanjiView.loadRasters(colourRasters, texts, squareSize) }
}


fun displayMatrix(image: Matrix<Boolean>, squareSize: Int = 1) {
    val startThread = Thread {
        try {
            Application.launch(RasterOverviewApplication::class.java)
        } catch (e: IllegalStateException) {
            if (log.isDebugEnabled) {
                log.debug(e.message, e)
            }
        }
    }
    startThread.start()

    val colourArrays = image.array.map {
        it.map {
            if (it) {
                Color.WHITE
            } else {
                Color.BLACK
            }
        }.toTypedArray()
    }.toTypedArray()

    val kanjiView = FX.find(KanjiView::class.java)
    FX.runAndWait { kanjiView.loadRasters(listOf(colourArrays), squareSize = squareSize) }
}


fun displayColourMatrix(image: Matrix<Color>, squareSize: Int = 1) {
    val startThread = Thread {
        try {
            Application.launch(RasterOverviewApplication::class.java)
        } catch (e: IllegalStateException) {
            if (log.isDebugEnabled) {
                log.debug(e.message, e)
            }
        }
    }
    startThread.start()

    val colourArrays = image.array.map {
        it.map {
            it
        }.toTypedArray()
    }.toTypedArray()

    val kanjiView = FX.find(KanjiView::class.java)
    FX.runAndWait { kanjiView.loadRasters(listOf(colourArrays), squareSize = squareSize) }
}


fun displayRasters(colourRasters: Collection<Array<Array<Color>>>, texts: List<String> = emptyList()) {
    val startThread = Thread {
        try {
            Application.launch(RasterOverviewApplication::class.java)
        } catch (e: IllegalStateException) {
            if (log.isDebugEnabled) {
                log.debug(e.message, e)
            }
        }
    }
    startThread.start()

    val kanjiView = FX.find(KanjiView::class.java)
    FX.runAndWait { kanjiView.loadRasters(colourRasters, texts) }
}


fun loadKanjisFromDirectory(path: Path, limit: Long = Long.MAX_VALUE): List<EncodedKanji> {
    return Files.list(path)
            .limit(limit)
            .map {
                loadEncodedKanji(it)
            }
            .toList()
}

fun loadEncodedKanji(path: Path, unicodeFunction: (String) -> Int = { name -> name.substring(0, name.indexOf('.')).toInt() }): EncodedKanji {
    val fileName = path.fileName.toString()
    return EncodedKanji(Files.readAllLines(path).map {
        it.map {
            if (it.equals('1')) {
                true
            } else if (it.equals('0')) {
                false
            } else {
                null
            }
        }
                .filterNotNull()
                .toBooleanArray()
    }.toTypedArray(), unicodeFunction(fileName))
}


fun main(args: Array<String>) {
//    val entries = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toList<KanjiDicParser.KanjiDicEntry>())
//    val charactersFoundInFile = extractCharacters(entries)
//
//    val encodedKanjis = FileInputStream(Parsers.FONT_FILE_LOCATION.toFile()).use({ fontStream ->
//        FontFileParser.parseFontFile(charactersFoundInFile, fontStream).stream().limit(1)
//    }).collect(Collectors.toList())
//
//    encodedKanjis.forEach {
//        it.printKanji()
//    }
//
//    displayKanjis(encodedKanjis)

    displayKanjis(loadKanjisFromDirectory(Paths.get("kanji_output8"), 50))

}