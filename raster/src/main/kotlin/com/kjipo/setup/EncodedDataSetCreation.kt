package com.kjipo.setup

import com.kjipo.representation.Matrix
import org.slf4j.LoggerFactory


private val logger = LoggerFactory.getLogger("EncodedDataSetCreation")

/*
fun applySegmentation(outputDir: Path) {
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
                    Pair(character.toInt(), EncodedKanji(FontFileParser.paintOnRaster(glyphVector, FontFileParser.NUMBER_OF_ROWS, FontFileParser.NUMBER_OF_COLUMNS)))
                }
            }).filter({ it != null })
            .collect(Collectors.toMap({ it -> it!!.first }, { it -> it!!.second },
                    { old, new ->
                        logger.warn("Found duplicate. Old: $old. New: $new")
                        new
                    }))

    unicodeKanjiMap.forEach({
        val unicode = it.key
        val encodedKanji = it.value

        val filteredKanji = makeThin(encodedKanji.image)

        Files.newBufferedWriter(outputDir.resolve(unicode.toString().plus(".dat")), StandardCharsets.UTF_8).use {
            it.write(transformKanjiData(filteredKanji, FontFileParser.NUMBER_OF_ROWS, FontFileParser.NUMBER_OF_COLUMNS))
        }
    })

}
*/

fun transformKanjiData(image: Matrix<Boolean>, maxRow: Int = image.numberOfRows, maxColumn: Int = image.numberOfColumns): String {
    val stringBuilder = StringBuilder()

    for (row in 0 until maxRow) {
        for (column in 0 until maxColumn) {
            if (row >= image.numberOfRows || column >= image.numberOfColumns) {
                stringBuilder.append("0").append(",")
            } else {
                stringBuilder.append(if (image[row, column]) 1 else 0).append(",")
            }
        }
        stringBuilder.delete(stringBuilder.length - 1, stringBuilder.length)
        stringBuilder.append("\n")
    }
    return stringBuilder.toString()
}