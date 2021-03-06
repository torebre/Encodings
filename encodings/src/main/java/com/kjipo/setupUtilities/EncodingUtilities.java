package com.kjipo.setupUtilities;

import com.kjipo.parser.KanjiDicParser;
import com.kjipo.parser.Parsers;
import com.kjipo.representation.EncodedKanji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kjipo.parser.FontFileParser.parseFontFile;

public final class EncodingUtilities {

    private static final Logger logger = LoggerFactory.getLogger(EncodingUtilities.class);

    private EncodingUtilities() {

    }


    public static void writeCharactersToFile(Iterable<EncodedKanji> kanjiData, Path outputFile) throws IOException {
        int maxRow = 100;
        int maxColumn = 100;

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            Iterator<EncodedKanji> iterator = kanjiData.iterator();
            while (iterator.hasNext()) {
                bufferedWriter.write(transformKanjiData(iterator.next(), maxRow, maxColumn));
            }
        }
    }

    public static String transformKanjiData(EncodedKanji encodedKanji, int maxRow, int maxColumn) {
        return transformKanjiData(encodedKanji.getImage(), maxRow, maxColumn);
    }

    public static String transformKanjiData(boolean booleanEncoding[][], int maxRow, int maxColumn) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int row = 0; row < maxRow; ++row) {
            for (int column = 0; column < maxColumn; ++column) {
                if (row >= booleanEncoding.length || column >= booleanEncoding[0].length) {
                    stringBuilder.append("0").append(",");
                } else {
                    stringBuilder.append(booleanEncoding[row][column] ? 0 : 1).append(",");
                }
            }
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }


    public static void main(String args[]) throws IOException, FontFormatException {
        java.util.List<KanjiDicParser.KanjiDicEntry> entries = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toList());

        Set<Integer> charactersFoundInFile = new HashSet<>();
        for (KanjiDicParser.KanjiDicEntry entry : entries) {

            for (int i = 0; i < entry.getKanji().length(); ++i) {
                charactersFoundInFile.add(entry.getKanji().codePointAt(i));
            }

//            char[] chars = entry.getKanji().toCharArray();
//            for (char character : chars) {
//                charactersFoundInFile.add(character);
//            }
        }

        logger.info("Number of characters found: {}", charactersFoundInFile.size());

        Path outputFile = Paths.get("/home/student/encodedkanji.txt");
        try (InputStream fontStream = new FileInputStream(Parsers.FONT_FILE_LOCATION.toFile())) {
            Collection<EncodedKanji> encodedKanjis = parseFontFile(charactersFoundInFile, fontStream, 200, 200);
            writeCharactersToFile(encodedKanjis, outputFile);
        }
    }

}
