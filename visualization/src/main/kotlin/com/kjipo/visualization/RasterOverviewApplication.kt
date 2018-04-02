package com.kjipo.visualization

import com.kjipo.parser.KanjiDicParser
import com.kjipo.parser.Parsers
import com.kjipo.prototype.CreatePrototypeDataset.extractCharacters
import com.kjipo.representation.EncodedKanji
import javafx.application.Application
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import tornadofx.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.streams.toList


class RasterOverviewApplication : App() {
    override val primaryView = KanjiView::class


}

val log = LoggerFactory.getLogger(RasterOverviewApplication::class.java)


fun displayKanjis(encodedKanjis: Collection<EncodedKanji>) {
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
    FX.runAndWait { kanjiView.loadRasters(colourRasters, characters) }
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


fun loadTestData() {
    val entries = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toList<KanjiDicParser.KanjiDicEntry>())
    val charactersFoundInFile = extractCharacters(entries)

    Files.walk(Paths.get("fittedPrototypes3"))


}


fun loadKanjisFromDirectory(path: Path, limit: Long = Long.MAX_VALUE): List<EncodedKanji> {
    return Files.list(path)
            .limit(limit)
            .map {
                val fileName = it.fileName.toString()

                EncodedKanji(Files.readAllLines(it).map {
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
                        }.toTypedArray(),
                        fileName.substring(0, fileName.indexOf('.')).toInt())
            }.toList()
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
//
//    }
//
//    displayKanjis(encodedKanjis)

    displayKanjis(loadKanjisFromDirectory(Paths.get("kanji_output2"), 50))


}