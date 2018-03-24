package com.kjipo.visualization

import com.kjipo.parser.FontFileParser
import com.kjipo.parser.KanjiDicParser
import com.kjipo.parser.Parsers
import com.kjipo.prototype.CreatePrototypeDataset.extractCharacters
import com.kjipo.representation.EncodedKanji
import javafx.application.Application
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import tornadofx.*
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors


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
        characters.add(it.character.toString())

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

fun displayRasters(colourRasters: Collection<Array<Array<Color>>>, texts:List<String> = emptyList()) {
    val startThread = Thread {
        try {
            Application.launch(RasterOverviewApplication::class.java)
        }
        catch(e:IllegalStateException) {
            if(log.isDebugEnabled) {
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


fun main(args: Array<String>) {
    val entries = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toList<KanjiDicParser.KanjiDicEntry>())
    val charactersFoundInFile = extractCharacters(entries)

    val encodedKanjis = FileInputStream(Parsers.FONT_FILE_LOCATION.toFile()).use({ fontStream ->
        FontFileParser.parseFontFile(charactersFoundInFile, fontStream).stream().limit(1)
    }).collect(Collectors.toList())

    encodedKanjis.forEach {
        it.printKanji()

    }

    displayKanjis(encodedKanjis)


}