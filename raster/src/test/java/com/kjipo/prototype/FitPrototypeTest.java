package com.kjipo.prototype;

import com.google.common.collect.ImmutableList;
import com.kjipo.raster.filter.Filter;
import com.kjipo.raster.filter.MaskFilter;
import com.kjipo.raster.segment.Pair;
import com.kjipo.representation.EncodedKanji;
import com.kjipo.segmentation.ColorCell;
import com.kjipo.segmentation.ColorPainter;
import com.kjipo.visualization.RasterRun;
import com.kjipo.visualization.RasterVisualizer2;
import javafx.scene.paint.Color;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FitPrototypeTest {


    @Test
    public void fitPrototypeTest() throws IOException, ClassNotFoundException, InterruptedException {
        EncodedKanji encodedKanji;
        try (InputStream fontStream = new FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile());
             ObjectInputStream objectInputStream = new ObjectInputStream(fontStream)) {
            encodedKanji = (EncodedKanji) objectInputStream.readObject();
        }

//        Filter maskFilter = new MaskFilter();
//        List<boolean[][]> results = maskFilter.applyFilter(encodedKanji.getImage());
//        boolean filteredImage[][] = results.get(results.size() - 1);

        FitPrototype fitPrototype = new FitPrototype();
        List<Collection<Prototype>> prototypes = fitPrototype.fit(encodedKanji.getImage());

        showRaster(encodedKanji.getImage(), prototypes);

        Thread.sleep(Long.MAX_VALUE);
    }


    private void showRaster(boolean encodedKanji[][], List<Collection<Prototype>> prototypeDevelopment) throws InterruptedException {
        RasterVisualizer2.showRasterFlow(
                new RasterRun<ColorCell>() {
                    private int current = 0;

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
