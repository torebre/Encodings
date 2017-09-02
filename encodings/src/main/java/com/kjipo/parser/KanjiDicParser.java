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
        return Files.lines(file, Parsers.JAPANESE_CHARSET).map(line -> {
            String[] splitLine = line.split("/");


            List<String> meanings = Arrays.asList(splitLine).subList(1, splitLine.length - 2);
//            Arrays.stream(line.split(" "))
//                    .filter(line2 -> line2.startsWith("{"))
//                    .map(line3 -> line3.substring(1, line3.length() - 1))
//                    .collect(Collectors.toList());
            String identifier = splitLine[splitLine.length - 1];

            return new KanjiDicEntry(splitLine[0],
                    meanings,
                    identifier);
        });
    }


    public static class KanjiDicEntry {
        private final String kanji;
        private final List<String> meanings;
        private final String identifier;


        public KanjiDicEntry(String kanji, List<String> meanings, String identifier) {
            this.kanji = kanji;
            this.meanings = Collections.unmodifiableList(meanings);
            this.identifier = identifier;
        }

        public String getKanji() {
            return kanji;
        }

        public List<String> getMeanings() {
            return meanings;
        }

        @Override
        public String toString() {
            return "KanjiDicEntry{" +
                    "kanji='" + kanji + '\'' +
                    ", meanings=" + meanings +
                    ", identifier='" + identifier + '\'' +
                    '}';
        }

        public String getIdentifier() {
            return identifier;
        }
    }


    public static void main(String args[]) throws IOException {
        List<KanjiDicEntry> entries = parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toList());

//        entries.forEach(System.out::println);

        KanjiDicEntry next = entries.iterator().next();
        System.out.println("Length: " +next.getKanji().length() +". Characters: " +next.getKanji().getBytes());


    }


}
