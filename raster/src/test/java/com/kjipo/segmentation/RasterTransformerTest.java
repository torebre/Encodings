package com.kjipo.segmentation;


import com.google.common.collect.ImmutableList;
import com.kjipo.parser.FontFileParser;
import com.kjipo.parser.KanjiDicParser;
import com.kjipo.parser.Parsers;
import com.kjipo.raster.Cell;
import com.kjipo.raster.filter.Filter;
import com.kjipo.raster.filter.MaskFilter;
import com.kjipo.representation.EncodedKanji;
import com.kjipo.visualization.CellType;
import com.kjipo.visualization.RasterElementProcessor;
import com.kjipo.visualization.RasterRun;
import com.kjipo.visualization.RasterVisualizer2;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
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

    public class FlowCell implements CellType {
        private final int row;
        private final int column;
        private final Cell cell;

        public FlowCell(int row, int column, Cell cell) {
            this.row = row;
            this.column = column;
            this.cell = cell;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        public Cell getCell() {
            return cell;
        }

    }


    public class CellFlowPainter implements RasterElementProcessor<FlowCell> {

        @Override
        public void processCell(FlowCell cell, int squareSize, ObservableList<Node> node, javafx.scene.shape.Rectangle rectangle) {
            if (cell.getCell().getFlowStrength() > 0) {
                rectangle.setFill(Color.RED);
            }
        }
    }

    public class FlowDirectionPainter implements RasterElementProcessor<FlowCell> {

        @Override
        public void processCell(FlowCell cell, int squareSize, ObservableList<Node> node, Rectangle rectangle) {
            if (cell.getCell().getFlowDirection() == null) {
                return;
            }
            double angle = cell.getCell().getFlowDirection().getAngleInRadians();

            Polyline arrow = new Polyline(-0.5, 0.0,
                    0.5, 0.0,
                    0.45, 0.05,
                    0.45, -0.05,
                    0.5, 0.0);
            arrow.setStrokeWidth(0.05);
            Affine affine = new Affine();

            if (angle < 0) {
                angle += 2 * Math.PI;
            } else if (angle > 2 * Math.PI) {
                angle -= 2 * Math.PI;
            }


            affine.appendRotation(360 - (360 * angle) / (2 * Math.PI));
            arrow.translateXProperty().setValue(cell.getColumn() * squareSize + squareSize / 2);
            arrow.translateYProperty().setValue(cell.getRow() * squareSize + squareSize / 2);

            affine.appendScale(squareSize, squareSize);
            arrow.getTransforms().add(affine);

            node.add(arrow);
        }
    }


    public static void main(String args[]) throws IOException, FontFormatException, JAXBException {
        java.util.List<KanjiDicParser.KanjiDicEntry> entries = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toList());

        Set<Character> charactersFoundInFile = new HashSet<>();
        for (KanjiDicParser.KanjiDicEntry entry : entries) {
            char[] chars = entry.getKanji().toCharArray();
            for (char character : chars) {
                charactersFoundInFile.add(character);
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