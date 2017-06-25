package com.kjipo.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KanjiDicParser {

    public static Stream<KanjiDicEntry> parseKanjidicFile(Path file) throws IOException {
        return Files.lines(file, Parsers.JAPANESE_CHARSET).map(line -> new KanjiDicEntry(line.substring(0, line.indexOf(' ')),
                Arrays.stream(line.split(" ")).filter(line2 -> line2.startsWith("{"))
                        .map(line3 -> line3.substring(1, line3.length() - 1)).collect(Collectors.toList())));
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
            return "Kanji: " + getKanji() + ". Meanings: " + getMeanings();
        }
    }


    public static void main(String args[]) throws IOException {
        List<KanjiDicEntry> entries = parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toList());

//        entries.forEach(System.out::println);

        KanjiDicEntry next = entries.iterator().next();
        System.out.println("Length: " +next.getKanji().length() +". Characters: " +next.getKanji().getBytes());


    }


}
