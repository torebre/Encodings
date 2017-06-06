package com.kjipo.representation;


import com.kjipo.utilities.Parsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.awt.*;
import java.io.*;
import java.util.stream.Stream;


public class LoadKanji {
    private static final Logger logger = LoggerFactory.getLogger(LoadKanji.class);



    @Test
    public void loadKanji() throws Exception {
        File file = new File("/home/student/Downloads/kanjidic");
        Font font = Parsers.loadKanjiFont();

        Stream<Parsers.KanjiDicEntry> entries = Parsers.parseKanjidicFile(file);
//        entries.forEach(entry -> logger.info("Kanji: {}", entry));


        Parsers.parseTrueTypeFontFile(entries.map(entry -> entry.getKanji()), font)
                .limit(5)
//                .forEach(encodedKanji -> encodedKanji.printKanji());
        .forEach(EncodedKanji::printKanji);
//                .forEach(entry -> logger.info("Encoded kanji:\n{}", entry));

        entries.close();

    }



}
