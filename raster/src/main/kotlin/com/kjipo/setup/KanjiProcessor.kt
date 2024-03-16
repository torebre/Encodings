package com.kjipo.setup

import com.kjipo.parser.FontFileParser
import com.kjipo.parser.KanjiDicParser
import com.kjipo.parser.Parsers
import com.kjipo.representation.EncodedKanji
import com.kjipo.representation.raster.makeThin
import com.kjipo.skeleton.transformArraysToMatrix
import org.slf4j.LoggerFactory
import java.awt.Font
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors


private val logger = LoggerFactory.getLogger("KanjiProcessor")

private val numberOfRows = 400
private val numberOfColumns = 400


fun processDictionaryFile(outputDir: Path, makeThinImage: Boolean = true) {
    if(!Files.exists(outputDir)) {
        Files.createDirectories(outputDir)
    }
    val parseKanjidicFile = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION)

    val characterKanjiMap: Map<String, KanjiDicParser.KanjiDicEntry> = parseKanjidicFile.map { Pair(it.identifier, it) }
            .collect(Collectors.toMap({ it.first }, { it.second }))

    val charactersFoundInFile = characterKanjiMap.values.flatMap {
        it.kanji.codePoints().boxed().collect(Collectors.toList())
    }.distinct()

    logger.info("Number of kanjis found: ${characterKanjiMap.size}")
    logger.info("Number of characters found: ${charactersFoundInFile.size}")

    val font = Files.newInputStream(Parsers.FONT_FILE_LOCATION).use {
        Font.createFont(Font.TRUETYPE_FONT, it)
    }

    val fontRenderContext = FontFileParser.getFontRenderContext()

    val unicodeKanjiMap: Map<Int, EncodedKanji> = charactersFoundInFile.stream()
            .map { unicode ->
                logger.info("Character: $unicode")
                Pair(unicode,
                    FontFileParser.createEncodedKanji(unicode, font, fontRenderContext, numberOfRows, numberOfColumns)
                )
            }.filter { it != null }
        .collect(Collectors.toMap({ it!!.first }, { it!!.second },
                    { old, new ->
                        logger.warn("Found duplicate. Old: $old. New: $new")
                        new}))

    unicodeKanjiMap.forEach {
        val unicode = it.key
        val encodedKanji = it.value

        val transformedKanji = if(makeThinImage) {
            makeThin(encodedKanji.image)
        }
        else {
            transformArraysToMatrix(encodedKanji.image)
        }

        Files.newBufferedWriter(outputDir.resolve(unicode.toString().plus(".dat")), StandardCharsets.UTF_8).use {
            it.write(transformKanjiData(transformedKanji, numberOfRows, numberOfColumns))
        }
    }
}


fun main(args: Array<String>) {
    processDictionaryFile(Paths.get("/home/student/workspace/testEncodings/kanji_output8_raw"), false)
}