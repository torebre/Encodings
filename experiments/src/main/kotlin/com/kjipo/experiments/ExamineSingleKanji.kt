package com.kjipo.experiments

import com.kjipo.parser.FontFileParser
import com.kjipo.parser.Parsers
import com.kjipo.visualization.displayKanjis
import java.io.FileInputStream
import java.util.*
import kotlin.streams.toList


fun showKanji() {
    val unicode = 24011
    val kanji = FileInputStream(Parsers.FONT_FILE_LOCATION.toFile()).use({ fontStream ->
        FontFileParser.parseFontFileUsingUnicodeInput(Collections.singleton(unicode), fontStream, 400, 400).stream()
    }).toList()

    displayKanjis(kanji, 1)
}


fun main(args: Array<String>) {
    showKanji()

}