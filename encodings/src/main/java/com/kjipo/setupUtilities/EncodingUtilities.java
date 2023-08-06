package com.kjipo.setupUtilities;

import com.kjipo.parser.KanjiDicParser;
import com.kjipo.parser.Parsers;
import com.kjipo.representation.EncodedKanji;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

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


    public static void writeEncodedKanjiToFiles(Collection<EncodedKanji> encodedKanjis, Path outputDirectory) {
        var encoder = Base64.getEncoder();

        encodedKanjis.parallelStream().forEach(encodedKanji -> {
            var fileToWriteTo = outputDirectory.resolve(encodedKanji.getUnicode() + ".dat");
            BitSet bitSet = getBitSet(encodedKanji);
            var base64EncodedData = encoder.encode(bitSet.toByteArray());

            try (FileOutputStream fileOutputStream = new FileOutputStream(fileToWriteTo.toFile())) {
                fileOutputStream.write(base64EncodedData);

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    @NotNull
    private static BitSet getBitSet(EncodedKanji encodedKanji) {
        boolean[][] data = encodedKanji.getImage();

        int bitsNeededForData = data.length * data[0].length;
        if (bitsNeededForData % 8 != 0) {
            bitsNeededForData += 1;
        }

        byte[] outputData = new byte[2 + bitsNeededForData];
        // The matrix has dimensions smaller than 256 so the length fits in a byte
        outputData[0] = (byte) data.length;
        outputData[1] = (byte) data[0].length;

        BitSet bitSet = BitSet.valueOf(outputData);

        for (int row = 0; row < data.length; ++row) {
            for (int column = 0; column < data[0].length; ++column) {
                // Note that data from data is being rotated here
                bitSet.set(16 + row * data.length + column, data[column][row]);
            }
        }
        return bitSet;
    }

    private static Set<Integer> getKanjiUniCodesFromFile() throws IOException {
        java.util.List<KanjiDicParser.KanjiDicEntry> entries = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).toList();

        Set<Integer> charactersFoundInFile = new HashSet<>();
        for (KanjiDicParser.KanjiDicEntry entry : entries) {
            for (int i = 0; i < entry.getKanji().length(); ++i) {
                charactersFoundInFile.add(entry.getKanji().codePointAt(i));
            }
        }

        logger.info("Number of characters found: {}", charactersFoundInFile.size());

        return charactersFoundInFile;
    }


    private static void writeKanjiFromDictionaryToFiles() throws IOException, FontFormatException {
        var charactersFoundInFile = getKanjiUniCodesFromFile();
        var outputDirectory = Path.of("/home/student/workspace/testEncodings/temp/kanjiOutput");

        List<EncodedKanji> encodedKanjis;
        try (InputStream fontStream = new FileInputStream(Parsers.FONT_FILE_LOCATION.toFile())) {
            encodedKanjis = parseFontFile(charactersFoundInFile, fontStream, 200, 200);
//            encodedKanjis = parseFontFile(Collections.singletonList(32769), fontStream, 200, 200);
        }

        writeEncodedKanjiToFiles(encodedKanjis, outputDirectory);
    }


    private static void writeAllKanjiToSingleFile() throws IOException, FontFormatException {
        var charactersFoundInFile = getKanjiUniCodesFromFile();

        Path outputFile = Paths.get("/home/student/encodedkanji.txt");
        try (InputStream fontStream = new FileInputStream(Parsers.FONT_FILE_LOCATION.toFile())) {
            Collection<EncodedKanji> encodedKanjis = parseFontFile(charactersFoundInFile, fontStream, 200, 200);
            writeCharactersToFile(encodedKanjis, outputFile);
        }

    }


    public static void main(String args[]) throws IOException, FontFormatException {
//        writeAllKanjiToSingleFile();
        writeKanjiFromDictionaryToFiles();
    }

}
