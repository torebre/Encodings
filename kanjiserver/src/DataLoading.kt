package com.kjipo

import com.kjipo.representation.EncodedKanji
import com.kjipo.representation.Line
import com.kjipo.representation.SegmentLine
import java.nio.file.Files


internal fun loadLines(): Map<Pair<Int, Int>, List<Line>> {
    val allLines = Files.readAllLines(Constants.segmentData)
    return allLines.subList(1, allLines.size)
            .map {
                val splitString = it.split(",")
                val key = splitString[0].toInt()
                val segment = splitString[6].toInt()

                Pair(Pair(key, segment), Line(splitString[1].toInt(),
                        splitString[2].toDouble(),
                        splitString[3].toDouble(),
                        splitString[4].toInt(),
                        splitString[5].toInt()))
            }
            .groupBy {
                it.first
            }.map {
                Pair(it.key, it.value.map { it.second })
            }.toMap()
}

internal fun loadSegmentLines(): Map<Int, List<SegmentLine>> {
    val allLines = Files.readAllLines(Constants.segmentData)
    return allLines.subList(1, allLines.size)
            .map {
                val splitString = it.split(",")
                SegmentLine(splitString[0].toInt(),
                        splitString[1].toInt(),
                        splitString[2].toDouble(),
                        splitString[3].toDouble(),
                        splitString[4].toInt(),
                        splitString[5].toInt(),
                        splitString[6].toInt())
            }
            .groupBy {
                it.unicode
            }
}


fun loadEncodedKanjiFromString(kanjiString: String, unicode: Int) = EncodedKanji(loadEncodedKanjiFromString(kanjiString.split('\n')), unicode)

fun loadEncodedKanjiFromString(kanjiString: List<String>): Array<BooleanArray> {
    return kanjiString.map {
        it.map {
            if (it == '1') {
                true
            } else if (it == '0') {
                false
            } else {
                null
            }
        }
                .filterNotNull()
                .toBooleanArray()
    }.toTypedArray()
}
