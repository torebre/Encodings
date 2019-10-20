package com.kjipo.prototype;


import com.kjipo.parser.KanjiDicParser;
import com.kjipo.parser.Parsers;
import com.kjipo.raster.segment.Segment;
import kotlin.Pair;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CreatePrototypeDatasetTest {


    @Test
    public void parseTest() throws IOException {
        Map<String, KanjiDicParser.KanjiDicEntry> identifierEntryMap = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION)
                .collect(Collectors.toMap(KanjiDicParser.KanjiDicEntry::getIdentifier, Function.identity()));

        Map<KanjiDicParser.KanjiDicEntry, Prototype> fittedPrototypes = Files.walk(Paths.get("fittedPrototypes3"))
                .filter(path -> Files.isRegularFile(path))
                .map(path -> {
                    try (InputStream inputStream = Files.newInputStream(path)) {
                        String fileName = path.getFileName().toString();
                        KanjiDicParser.KanjiDicEntry kanjiDicEntry = identifierEntryMap.get(fileName.substring(0, fileName.indexOf('.')));

                        return new Pair<>(kanjiDicEntry, CreatePrototypeDataset.readPrototype(inputStream));

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));






        fittedPrototypes.entrySet().forEach(entry -> {
            for (Segment segment : entry.getValue().getSegments()) {
//                segment.getPairs().

            }


        });


//        prototypes.forEach(System.out::println);


    }

}