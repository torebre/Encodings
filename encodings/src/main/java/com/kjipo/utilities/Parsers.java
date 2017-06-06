package com.kjipo.utilities;


import com.kjipo.representation.EncodedKanji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kjipo.setupUtilities.RasterUtilities;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Parsers {

    private static final Logger logger = LoggerFactory.getLogger(Parsers.class);



    public static void parseFile(File file) throws IOException {

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(new File("/home/student/Downloads/kanjidic"))), "EUC-JP"));
        ) {

            while(reader.readLine() != null) {
//            testCharacter = reader.readLine().substring(0, 1);
                logger.info(reader.readLine());

            }


        }

    }


    public static Stream<KanjiDicEntry> parseKanjidicFile(File file) throws IOException {
        return Files.lines(file.toPath(), Charset.forName("EUC_JP")).map(line -> new KanjiDicEntry(line.substring(0, line.indexOf(' ')),
                Arrays.stream(line.split(" ")).filter(line2 -> line2.startsWith("{"))
                        .map(line3 -> line3.substring(1, line3.length() - 1)).collect(Collectors.toList())));
    }

    public static Font loadKanjiFont() throws IOException, FontFormatException {
        try (
            InputStream inputStream = Parsers.class.getResourceAsStream("/font/kochi-mincho-subst.ttf");
        ) {
            return Font.createFont(Font.TRUETYPE_FONT, inputStream);
        }
    }


    public static Stream<EncodedKanji> parseTrueTypeFontFile(Stream<String> characters, Font font) throws IOException, FontFormatException {
        final FontRenderContext renderContext = new FontRenderContext(null, false, false);
        return characters.map(s -> new EncodedKanji(RasterUtilities.paintOnRaster(
                font.createGlyphVector(renderContext, s), 100, 100)));
    }



    public static class KanjiDicEntry {
        private final String kanji;
        private final List<String> meanings;


        public KanjiDicEntry(String kanji, List<String> meanings) {
            this.kanji = kanji;
            this.meanings = Collections.unmodifiableList(meanings);
        }

        public String getKanji() {
            return kanji;
        }

        public List<String> getMeanings() {
            return meanings;
        }

        @Override
        public String toString() {
            return "Kanji: " +getKanji() +". Meanings: " +getMeanings();
        }

    }


}
