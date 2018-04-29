package com.kjipo.experiments

import com.kjipo.parser.FontFileParser
import com.kjipo.parser.Parsers
import com.kjipo.representation.EncodedKanji
import com.kjipo.visualization.displayKanjis
import com.kjipo.visualization.loadKanjisFromDirectory
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors
import kotlin.streams.toList


fun showKanji() {
    val unicode = 24011
    val kanji = FileInputStream(Parsers.FONT_FILE_LOCATION.toFile()).use({ fontStream ->
        FontFileParser.parseFontFileUsingUnicodeInput(Collections.singleton(unicode), fontStream, 400, 400).stream()
    }).toList()

    displayKanjis(kanji, 1)
}


fun showKanji2() {
    val encodedKanjis = loadKanjisFromDirectory(Paths.get("kanji_output4")).stream()
            .collect(Collectors.toMap<EncodedKanji, Int, EncodedKanji>({
                it.unicode
            }) {
                it
            })


    displayKanjis(Collections.singleton(encodedKanjis.getOrDefault(24011, EncodedKanji(emptyArray(), 0))))



}


fun main(args: Array<String>) {
    showKanji2()

}