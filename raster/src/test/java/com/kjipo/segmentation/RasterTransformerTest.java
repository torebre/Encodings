package com.kjipo.segmentation;


import com.google.common.collect.ImmutableList;
import com.kjipo.parser.FontFileParser;
import com.kjipo.parser.KanjiDicParser;
import com.kjipo.parser.Parsers;
import com.kjipo.raster.Cell;
import com.kjipo.raster.filter.Filter;
import com.kjipo.raster.filter.MaskFilter;
import com.kjipo.representation.EncodedKanji;
import com.kjipo.visualization.RasterRun;
import com.kjipo.visualization.RasterVisualizer2;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class RasterTransformerTest {
    private static final Logger logger = LoggerFactory.getLogger(RasterTransformerTest.class);


    @Test
    public void processKanjiTest() throws IOException, FontFormatException, InterruptedException, ClassNotFoundException {
        EncodedKanji encodedKanji;
        try (InputStream fontStream = new FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile());
             ObjectInputStream objectInputStream = new ObjectInputStream(fontStream)) {
            encodedKanji = (EncodedKanji) objectInputStream.readObject();
        }

        logger.info("Determining flow");

        Cell[][] flowRaster = RasterTransformer.segmentTransformer(encodedKanji.getImage());

        Filter maskFilter = new MaskFilter();
        boolean[][] results = maskFilter.applyFilter(encodedKanji.getImage()).get(0);

        RasterVisualizer2.showRasterFlow(
                new RasterRun<FlowCell>() {
                    private int current = 0;

                    @Override
                    public boolean[][] getRawInput() {
                        return encodedKanji.getImage();
//                        return results;
                    }

                    @Override
                    public boolean hasNext() {
                        return current < 2;
                    }

                    @Override
                    public int getColumns() {
                        return encodedKanji.getImage()[0].length;
                    }

                    @Override
                    public int getRows() {
                        return encodedKanji.getImage().length;
                    }

                    @Override
                    public FlowCell getCell(int row, int column) {
                        return new FlowCell(row, column, flowRaster[row][column]);
                    }

                    @Override
                    public void next() {
                        ++current;
                    }
                },
                ImmutableList.of(new CellFlowPainter(), new FlowDirectionPainter()));


        Thread.sleep(Long.MAX_VALUE);
    }


    public static void main(String args[]) throws IOException, FontFormatException {
        java.util.List<KanjiDicParser.KanjiDicEntry> entries = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toList());

        Set<Integer> charactersFoundInFile = new HashSet<>();
        for (KanjiDicParser.KanjiDicEntry entry : entries) {
            for (int i = 0; i < entry.getKanji().length(); ++i) {
                charactersFoundInFile.add(Character.codePointAt(entry.getKanji(), i));
            }
        }

        logger.info("Number of characters found: {}", charactersFoundInFile.size());

        EncodedKanji encodedKanji;
        try (InputStream fontStream = new FileInputStream(Parsers.FONT_FILE_LOCATION.toFile())) {
            Collection<EncodedKanji> encodedKanjis = FontFileParser.parseFontFile(charactersFoundInFile, fontStream);
            encodedKanji = encodedKanjis.iterator().next();
        }

//        JAXBContext jaxbContext = JAXBContext.newInstance(EncodedKanji.class);
//        Marshaller marshaller = jaxbContext.createMarshaller();
//        marshaller.marshal(encodedKanji, Paths.get("/home/student/test_kanji.xml").toFile());

        try (OutputStream outputStream = Files.newOutputStream(Paths.get("/home/student/test_kanji.xml"));
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(encodedKanji);
        }


    }


}