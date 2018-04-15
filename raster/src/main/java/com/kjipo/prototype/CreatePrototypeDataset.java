package com.kjipo.prototype;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.kjipo.parser.FontFileParser;
import com.kjipo.parser.KanjiDicParser;
import com.kjipo.parser.Parsers;
import com.kjipo.raster.attraction.PrototypeCollection;
import com.kjipo.raster.segment.Pair;
import com.kjipo.representation.EncodedKanji;
import com.kjipo.skeleton.BwmethodsKt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.stream.Collectors;

public class CreatePrototypeDataset {
    private static final Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AngleLine.class, new AngleLinePrototypeDeserializer());
        gson = gsonBuilder.create();
    }


    private static final Logger logger = LoggerFactory.getLogger(CreatePrototypeDataset.class);


    public void fitPrototypes2(Path outputDirectory, BiConsumer<Path, Prototype> serializerFunction) throws IOException, FontFormatException {
        prepareOutputDirectory(outputDirectory);

        java.util.List<KanjiDicParser.KanjiDicEntry> entries = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toList()).stream()
                .limit(20)
                .collect(Collectors.toList());
        Set<Integer> charactersFoundInFile = extractCharacters2(entries);

        FitPrototype fitPrototype = new FitPrototype();

        try (InputStream fontStream = new FileInputStream(Parsers.FONT_FILE_LOCATION.toFile())) {
            Collection<EncodedKanji> encodedKanjis = new ArrayList<>(FontFileParser.parseFontFileUsingUnicodeInput(charactersFoundInFile, fontStream, FontFileParser.NUMBER_OF_ROWS, FontFileParser.NUMBER_OF_COLUMNS));

            for (EncodedKanji encodedKanji : encodedKanjis) {
                try {
                    Pair topPair = Pair.of(0, 0);
                    int topId = 1;
                    AngleLine top = new AngleLine(topId, topPair, 3.0, 0);

                    List<AngleLine> allLines = Lists.newArrayList(top);

                    boolean[][] processedImage = BwmethodsKt.transformToArrays(BwmethodsKt.makeThin(encodedKanji.getImage()));

                    List<Prototype> prototypes = fitPrototype.addPrototypes(processedImage, allLines, false);
                    Path outputFile = outputDirectory.resolve(encodedKanji.getUnicode() + ".json");
                    serializerFunction.accept(outputFile, prototypes.get(prototypes.size() - 1));

                } catch (RuntimeException e) {
                    logger.error("Skipping character because of exception: {}", new String(Character.toChars(encodedKanji.getUnicode())), e);
                }

            }
        }


    }


    public static Set<Character> extractCharacters(java.util.List<KanjiDicParser.KanjiDicEntry> entries) {
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


    private static Set<Integer> extractCharacters2(java.util.List<KanjiDicParser.KanjiDicEntry> entries) {
        // See http://www.rikai.com/library/kanjitables/kanji_codes.unicode.shtml for a list of unicode ranges for Japanese characters
        Set<Integer> charactersFoundInFile = new HashSet<>();
        for (KanjiDicParser.KanjiDicEntry entry : entries) {
            for (int i = 0; i < entry.getKanji().length(); ++i) {
                int unicode = Character.codePointAt(entry.getKanji(), i);
                charactersFoundInFile.add(unicode);
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
        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             JsonReader jsonReader = new JsonReader(inputStreamReader)) {

            jsonReader.beginArray();

            jsonReader.beginArray();


            AngleLine angleLine = gson.fromJson(jsonReader, AngleLine.class);

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
            jsonWriter.jsonValue(gson.toJson(prototype1));
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


    public static void main(String args[]) throws IOException, FontFormatException {
        CreatePrototypeDataset createPrototypeDataset = new CreatePrototypeDataset();
        createPrototypeDataset.fitPrototypes2(Paths.get("fittedPrototypes4"), getSerializer());

//        try (FileInputStream fs = new FileInputStream(Paths.get("fittedPrototypes/12406.dat").toFile())) {
//            readPrototype(fs);
//        }

    }


}
