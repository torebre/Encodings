package com.kjipo.prototype;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.filter.Filter;
import com.kjipo.raster.filter.MaskFilter;
import com.kjipo.raster.flow.BooleanEncodingTestData;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.representation.EncodedKanji;
import com.kjipo.segmentation.LineSegmentationKt;
import com.kjipo.skeleton.BwmethodsKt;
import com.kjipo.visualization.segmentation.ColorCell;
import com.kjipo.visualization.segmentation.ColorPainter;
import com.kjipo.visualization.RasterRun;
import com.kjipo.visualization.RasterVisualizer2;
import javafx.scene.paint.Color;
import org.junit.Test;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class FitPrototypeTest {


    @Test
    public void fitPrototypeTest() throws IOException, ClassNotFoundException, InterruptedException {
        EncodedKanji encodedKanji;
        try (InputStream fontStream = new FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile());
             ObjectInputStream objectInputStream = new ObjectInputStream(fontStream)) {
            encodedKanji = (EncodedKanji) objectInputStream.readObject();
        }

        Filter maskFilter = new MaskFilter();
        List<boolean[][]> results = maskFilter.applyFilter(encodedKanji.getImage());
        boolean filteredImage[][] = results.get(results.size() - 1);

        FitPrototype fitPrototype = new FitPrototype();
        List<Collection<Prototype>> prototypes = fitPrototype.fit(filteredImage);

        showRaster(encodedKanji.getImage(), prototypes);

        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void fitPrototypeTest3() throws InterruptedException {
        EncodedKanji encodedKanji = new EncodedKanji(BooleanEncodingTestData.getTestRaster6(50, 50), Character.codePointAt(new char[]{'a'}, 0));

        Filter maskFilter = new MaskFilter();
        List<boolean[][]> results = maskFilter.applyFilter(encodedKanji.getImage());
        boolean filteredImage[][] = results.get(results.size() - 1);


        Pair topPair = Pair.of(0, 0);
        int topId = 1;
        AngleLine top = new AngleLine(topId, topPair, 5.0, 0);

        int rightId = 2;
        AngleLine right = new AngleLine(rightId, null, 5.0, 0.5 * Math.PI);
        top.addConnectedTo(rightId);

        int bottomId = 3;
        AngleLine bottom = new AngleLine(bottomId, null, 5.0, 0.5 * Math.PI);
        right.addConnectedTo(bottomId);

        int leftId = 4;
        AngleLine left = new AngleLine(leftId, null, 5.0, 0.5 * Math.PI);
        bottom.addConnectedTo(leftId);

        int connectorId = 5;
        AngleLine connector = new AngleLine(connectorId, null, 10.0, -0.5 * Math.PI);
        bottom.addConnectedTo(connectorId);

        int underId = 6;
        AngleLine underLine = new AngleLine(underId, null, 3.0, -0.5 * Math.PI);
        connector.addConnectedTo(underId);

        List<AngleLine> allLines = Lists.newArrayList(top, right, bottom, left, connector, underLine);


        FitPrototype fitPrototype = new FitPrototype();
        List<List<Prototype>> prototypes = fitPrototype.addSinglePrototype2(filteredImage, allLines, 5, 5).stream()
                .map(Collections::singletonList)
                .collect(Collectors.toList());

        showRaster(filteredImage, prototypes);

        Thread.sleep(Long.MAX_VALUE);

    }


    @Test
    public void fitPrototypeTest4() throws InterruptedException, IOException, ClassNotFoundException {
        EncodedKanji encodedKanji;
        try (InputStream fontStream = new FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile());
             ObjectInputStream objectInputStream = new ObjectInputStream(fontStream)) {
            encodedKanji = (EncodedKanji) objectInputStream.readObject();
        }

        Filter maskFilter = new MaskFilter();
        List<boolean[][]> results = maskFilter.applyFilter(encodedKanji.getImage());
        boolean filteredImage[][] = results.get(results.size() - 1);


        Pair topPair = Pair.of(0, 0);
        int topId = 1;
        AngleLine top = new AngleLine(topId, topPair, 3.0, 0);

        int rightId = 2;
        AngleLine right = new AngleLine(rightId, null, 3.0, 0.5 * Math.PI);
        top.addConnectedTo(rightId);

        int bottomId = 3;
        AngleLine bottom = new AngleLine(bottomId, null, 3.0, 0.5 * Math.PI);
        right.addConnectedTo(bottomId);

        int leftId = 4;
        AngleLine left = new AngleLine(leftId, null, 3.0, 0.5 * Math.PI);
        bottom.addConnectedTo(leftId);

        int connectorId = 5;
        AngleLine connector = new AngleLine(connectorId, null, 3.0, 1.5 * Math.PI);
        bottom.addConnectedTo(connectorId);

        int underId = 6;
        AngleLine underLine = new AngleLine(underId, null, 3.0, 0);
        connector.addConnectedTo(underId);

        List<AngleLine> allLines = Lists.newArrayList(top, right, bottom, left, connector, underLine);


        FitPrototype fitPrototype = new FitPrototype();
//        List<List<Prototype>> prototypes = fitPrototype.addSinglePrototype2(filteredImage, allLines).stream()
        List<List<Prototype>> prototypes = fitPrototype.addPrototypes(filteredImage, allLines).stream()
                .map(Collections::singletonList)
                .collect(Collectors.toList());

        showRaster(filteredImage, prototypes);

        Thread.sleep(Long.MAX_VALUE);

    }


    private void showRaster(boolean encodedKanji[][], List<? extends Collection<Prototype>> prototypeDevelopment) throws InterruptedException {
        RasterVisualizer2.showRasterFlow(
                new RasterRun<ColorCell>() {
                    private int current = -1;

                    @Override
                    public boolean[][] getRawInput() {
                        return encodedKanji;
                    }

                    @Override
                    public boolean hasNext() {
                        return current < prototypeDevelopment.size() - 1;
                    }

                    @Override
                    public int getColumns() {
                        return encodedKanji[0].length;
                    }

                    @Override
                    public int getRows() {
                        return encodedKanji.length;
                    }

                    @Override
                    public ColorCell getCell(int row, int column) {
                        List<Pair> pairs = prototypeDevelopment.get(current).stream()
                                .flatMap(prototype -> prototype.getSegments().stream())
                                .flatMap(segment -> segment.getPairs().stream())
                                .collect(Collectors.toList());

                        return new ColorCell(row, column, Color.BLUE, Collections.emptyList(), pairs);
                    }

                    @Override
                    public void next() {
                        ++current;
                    }
                },
                ImmutableList.of(new ColorPainter()));

    }


}
