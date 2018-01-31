package com.kjipo.setup

import com.kjipo.parser.FontFileParser
import com.kjipo.parser.KanjiDicParser
import com.kjipo.parser.Parsers
import com.kjipo.representation.EncodedKanji
import com.kjipo.setupUtilities.EncodingUtilities
import org.slf4j.LoggerFactory
import java.awt.Font
import java.awt.font.FontRenderContext
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors


private val logger = LoggerFactory.getLogger("KanjiProcessor")


fun processDictionaryFile(outputDir: Path) {
    val parseKanjidicFile = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION)

    val characterKanjiMap: Map<String, KanjiDicParser.KanjiDicEntry> = parseKanjidicFile.map { Pair(it.identifier, it) }
            .collect(Collectors.toMap({ it -> it.first }, { it.second }))

    val charactersFoundInFile = characterKanjiMap.values.map {
        it.kanji.toCharArray().toSet()
    }.flatten().toSet()

    logger.info("Number of kanjis found: ${characterKanjiMap.size}")
    logger.info("Number of characters found: ${charactersFoundInFile.size}")

    val font = FileInputStream(Parsers.FONT_FILE_LOCATION.toFile()).use { fontStream ->
        return@use Font.createFont(Font.TRUETYPE_FONT, fontStream)
    }

    val renderContext = FontRenderContext(null, false, false)

    val unicodeKanjiMap: Map<Int, EncodedKanji> = charactersFoundInFile.stream()
            .map({ character ->

                logger.info("Character: $character")

                val glyphVector = font.createGlyphVector(renderContext, charArrayOf(character))
                if (glyphVector.getNumGlyphs() > 1) {
                    logger.warn("Skipping character: $character ")
                    null
                } else {
                    Pair(character.toInt(), EncodedKanji(character, FontFileParser.paintOnRaster(glyphVector, FontFileParser.NUMBER_OF_ROWS, FontFileParser.NUMBER_OF_COLUMNS)))
                }
            }).filter({ it != null })
            .collect(Collectors.toMap({ it -> it!!.first }, { it -> it!!.second },
                    { old, new ->
                        logger.warn("Found duplicate. Old: $old. New: $new")
                        new}))

    unicodeKanjiMap.forEach({
        val unicode = it.key
        val encodedKanji = it.value
        Files.newBufferedWriter(outputDir.resolve(unicode.toString().plus(".dat")), StandardCharsets.UTF_8).use {
            it.write(EncodingUtilities.transformKanjiData(encodedKanji, FontFileParser.NUMBER_OF_ROWS, FontFileParser.NUMBER_OF_COLUMNS))
        }
    })

}


fun main(args: Array<String>) {
    processDictionaryFile(Paths.get("/home/student/workspace/testEncodings/kanji_output"))

}