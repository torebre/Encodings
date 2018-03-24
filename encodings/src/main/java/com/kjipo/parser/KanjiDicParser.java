package com.kjipo.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KanjiDicParser {

    public static Stream<KanjiDicEntry> parseKanjidicFile(Path file) throws IOException {
        return parseKanjidicFile(file, Integer.MAX_VALUE);
    }


    public static Stream<KanjiDicEntry> parseKanjidicFile(Path file, int maxLinesToRead) throws IOException {
        return Files.lines(file, Parsers.JAPANESE_CHARSET)
                .limit(maxLinesToRead)
                .map(line -> {
                    String[] splitLine = line.split("/");
                    List<String> meanings = Arrays.asList(splitLine).subList(1, splitLine.length - 2);
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

        Map<String, KanjiDicEntry> collect = parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toMap(KanjiDicEntry::getIdentifier, entry -> entry));

//        entries.forEach(System.out::println);

        KanjiDicEntry next = entries.iterator().next();
        System.out.println("Length: " + next.getKanji().length() + ". Characters: " + next.getKanji().getBytes());


    }


}
