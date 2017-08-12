package com.kjipo.segmentation;

import com.google.common.collect.ImmutableList;
import com.kjipo.raster.Cell;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.representation.EncodedKanji;
import com.kjipo.visualization.CellType;
import com.kjipo.visualization.RasterElementProcessor;
import com.kjipo.visualization.RasterRun;
import com.kjipo.visualization.RasterVisualizer2;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;

public class KanjiSegmenterTest {
    private static final Logger logger = LoggerFactory.getLogger(KanjiSegmenterTest.class);


    @Test
    public void segmentationTest() throws IOException, ClassNotFoundException, InterruptedException {
//        boolean prototype[][] = new boolean[10][10];
//        for (int i = 0; i < 6; ++i) {
//            prototype[8][i + 2] = true;
//        }
//        EncodedKanji encodedKanji = new EncodedKanji(prototype);


        EncodedKanji encodedKanji;
        try (InputStream fontStream = new FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile());
             ObjectInputStream objectInputStream = new ObjectInputStream(fontStream)) {
            encodedKanji = (EncodedKanji) objectInputStream.readObject();
        }


        logger.info("Determining flow");


        Cell[][] flowRaster = RasterTransformer.segmentTransformer(encodedKanji.getImage());

        List<Segment> segments = KanjiSegmenter.segmentKanji(flowRaster);

        logger.info("Number of segments: {}", segments.size());

        double red = 0.0;
        double blue = 0.0;

        double deltaRed = 1.0 / segments.size();
        double deltaBlue = 1.0 / segments.size();

        Color colorRaster[][] = new Color[flowRaster.length][flowRaster[0].length];
        for (Segment segment : segments) {
            Color segmentColor = Color.color(red, blue, 1.0);

            for (Pair pair : segment.getPairs()) {
                colorRaster[pair.getRow()][pair.getColumn()] = segmentColor;
            }

            red += deltaRed;
            blue += deltaBlue;
        }




        showRasterFlow(encodedKanji, colorRaster);

        Thread.sleep(Long.MAX_VALUE);

    }

    private static void showRasterFlow(EncodedKanji encodedKanji, Color colorRaster[][]) throws InterruptedException {
        RasterVisualizer2.showRasterFlow(
                new RasterRun<ColorCell>() {
                    private int current = 0;

                    @Override
                    public boolean[][] getRawInput() {
                        return encodedKanji.getImage();
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
                    public ColorCell getCell(int row, int column) {
                        return new ColorCell(colorRaster[row][column]);
                    }

                    @Override
                    public void next() {
                        ++current;
                    }
                },
                ImmutableList.of(new ColorPainter()));

    }


    private static class ColorCell implements CellType {
        private final Color color;


        public ColorCell(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }


    public static class ColorPainter implements RasterElementProcessor<ColorCell> {

        @Override
        public void processCell(ColorCell cell, int squareSize, ObservableList<Node> node, javafx.scene.shape.Rectangle rectangle) {
            rectangle.setFill(cell.getColor());
        }
    }


}
