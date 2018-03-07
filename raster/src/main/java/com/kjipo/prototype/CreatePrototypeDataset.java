package com.kjipo.prototype;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.stream.JsonWriter;
import com.kjipo.parser.FontFileParser;
import com.kjipo.parser.KanjiDicParser;
import com.kjipo.parser.Parsers;
import com.kjipo.raster.attraction.PrototypeCollection;
import com.kjipo.raster.segment.Pair;
import com.kjipo.representation.EncodedKanji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CreatePrototypeDataset {
    private static final Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AngleLine.class, new AngleLinePrototypeDeserializer());
        gson = gsonBuilder.create();
    }


    private static final Logger logger = LoggerFactory.getLogger(CreatePrototypeDataset.class);


    public void fitPrototypes(Path outputDirectory) throws IOException, FontFormatException {
        prepareOutputDirectory(outputDirectory);

        java.util.List<KanjiDicParser.KanjiDicEntry> entries = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toList());
        Set<Character> charactersFoundInFile = extractCharacters(entries);

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


    public void fitPrototypes2(Path outputDirectory, BiConsumer<Path, Prototype> serializerFunction) throws IOException, FontFormatException {
        prepareOutputDirectory(outputDirectory);

        // TODO Put back parsing of all characters
        java.util.List<KanjiDicParser.KanjiDicEntry> entries = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toList());
        Set<Character> charactersFoundInFile = extractCharacters(entries);

        FitPrototype fitPrototype = new FitPrototype();


//        JAXBContext jaxbContext = JAXBContext.newInstance(AngleLine.class, ArrayList.class, PrototypeCollection.class);
//        Marshaller marshaller = jaxbContext.createMarshaller();

        Pair topPair = Pair.of(0, 0);
        int topId = 1;
        AngleLine top = new AngleLine(topId, topPair, 3.0, 0);

        List<AngleLine> allLines = Lists.newArrayList(top);

        try (InputStream fontStream = new FileInputStream(Parsers.FONT_FILE_LOCATION.toFile())) {

            // TODO Put back parsing of all characters
            Collection<EncodedKanji> encodedKanjis = new ArrayList<>(FontFileParser.parseFontFile(charactersFoundInFile, fontStream));

            for (EncodedKanji encodedKanji : encodedKanjis) {
                try {
                    List<Prototype> prototypes = fitPrototype.addPrototypes(encodedKanji.getImage(), allLines, false);

                    Path outputFile = outputDirectory.resolve((int) encodedKanji.getCharacter() + ".json");

                    serializerFunction.accept(outputFile, prototypes.get(prototypes.size() - 1));

//                    try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
//                        marshaller.marshal(prototypeCollection, bufferedWriter);
//                    }

//                    Files.write(outputFile, gson.toJson(prototypes1).getBytes(StandardCharsets.UTF_8));
                } catch (RuntimeException e) {
                    logger.error("Skipping character because of exception: {}", encodedKanji.getCharacter(), e);
                }

            }
        }


    }


    private static Set<Character> extractCharacters(java.util.List<KanjiDicParser.KanjiDicEntry> entries) {
        // See http://www.rikai.com/library/kanjitables/kanji_codes.unicode.shtml for a list of unicode ranges for Japanese characters
        Set<Character> charactersFoundInFile = new HashSet<>();
        for (KanjiDicParser.KanjiDicEntry entry : entries) {
            char[] chars = entry.getKanji().toCharArray();
            for (char character : chars) {
                // Just include characters that are part of the Japanese range
                if (character > 2999) {
                    charactersFoundInFile.add(character);
                }
            }
        }
        return charactersFoundInFile;
    }


    private static void prepareOutputDirectory(Path outputDirectory) throws IOException {
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


    }


    public static Prototype readPrototype(InputStream inputStream) throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AngleLine.class, new AngleLinePrototypeDeserializer());

        Gson gson = new Gson();
        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            AngleLine angleLine = gson.fromJson(inputStreamReader, AngleLine.class);

            return angleLine;

        }


    }

    private static BiConsumer<Path, Prototype> getSerializer() {
        return (outputFile, prototype) -> {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                JsonWriter jsonWriter = gson.newJsonWriter(bufferedWriter);
                if (prototype instanceof PrototypeCollection) {
                    jsonWriter.beginArray();
                    handlePrototypeCollection((PrototypeCollection) prototype, jsonWriter, gson);
                    jsonWriter.endArray();
                } else {
                    throw new IllegalStateException("Unexpected structure");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }


    private static void handlePrototypeCollection(PrototypeCollection<?> prototypeCollection, JsonWriter jsonWriter, Gson gson) throws IOException {
        jsonWriter.beginArray();
        for (Object prototype1 : prototypeCollection.getPrototypes()) {
            if (prototype1 instanceof PrototypeCollection) {
                handlePrototypeCollection((PrototypeCollection) prototype1, jsonWriter, gson);
            } else if (!(prototype1 instanceof AngleLine)) {
                throw new IllegalArgumentException("Only AngleLine supported for JSON serializing. Class: " + prototype1.getClass());
            }
            jsonWriter.value(gson.toJson(prototype1));
        }
        jsonWriter.endArray();

    }


    public static class AngleLinePrototypeDeserializer implements InstanceCreator<AngleLine> {


        @Override
        public AngleLine createInstance(Type type) {

            System.out.println("Type: " + type);

            return null;
        }
    }


    public static void main(String args[]) throws IOException, FontFormatException, JAXBException {
        CreatePrototypeDataset createPrototypeDataset = new CreatePrototypeDataset();
        createPrototypeDataset.fitPrototypes2(Paths.get("fittedPrototypes2"), getSerializer());

//        try (FileInputStream fs = new FileInputStream(Paths.get("fittedPrototypes/12406.dat").toFile())) {
//            readPrototype(fs);
//        }

    }


}
