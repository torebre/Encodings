package com.kjipo.prototype;

import com.google.gson.Gson;
import com.kjipo.parser.FontFileParser;
import com.kjipo.parser.KanjiDicParser;
import com.kjipo.parser.Parsers;
import com.kjipo.representation.EncodedKanji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CreatePrototypeDataset {

    private static final Logger logger = LoggerFactory.getLogger(CreatePrototypeDataset.class);


    public void fitPrototypes(Path outputDirectory) throws IOException, FontFormatException, JAXBException {
        if (Files.exists(outputDirectory)) {
            Files.list(outputDirectory).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            });
            Files.deleteIfExists(outputDirectory);
        }
        Files.createDirectory(outputDirectory);


        java.util.List<KanjiDicParser.KanjiDicEntry> entries = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toList());

        Set<Character> charactersFoundInFile = new HashSet<>();
        for (KanjiDicParser.KanjiDicEntry entry : entries) {
            char[] chars = entry.getKanji().toCharArray();
            for (char character : chars) {
                charactersFoundInFile.add(character);
            }
        }

        FitPrototype fitPrototype = new FitPrototype();

        Gson gson = new Gson();

        try (InputStream fontStream = new FileInputStream(Parsers.FONT_FILE_LOCATION.toFile())) {
            Collection<EncodedKanji> encodedKanjis = FontFileParser.parseFontFile(charactersFoundInFile, fontStream);
            for (EncodedKanji encodedKanji : encodedKanjis) {
                List<Collection<Prototype>> fit = fitPrototype.fit(encodedKanji.getImage());
                Collection<Prototype> finalConfiguration = fit.get(fit.size() - 1);

                logger.info("Fit size: {}", fit.size());

                Path outputFile = outputDirectory.resolve((int) encodedKanji.getCharacter() + ".dat");
                Files.write(outputFile, gson.toJson(finalConfiguration).getBytes(StandardCharsets.UTF_8));
            }
        }



    }


    public static void main(String args[]) throws IOException, FontFormatException, JAXBException {
        CreatePrototypeDataset createPrototypeDataset = new CreatePrototypeDataset();
        createPrototypeDataset.fitPrototypes(Paths.get("fittedPrototypes"));
    }


}
