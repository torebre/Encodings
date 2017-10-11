package com.kjipo.prototype;

import com.google.common.collect.ImmutableList;
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

public class FitPrototypeTest {


    @Test
    public void fitPrototypeTest() throws IOException, ClassNotFoundException, InterruptedException {
        EncodedKanji encodedKanji;
        try (InputStream fontStream = new FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile());
             ObjectInputStream objectInputStream = new ObjectInputStream(fontStream)) {
            encodedKanji = (EncodedKanji) objectInputStream.readObject();
        }

        FitPrototype fitPrototype = new FitPrototype();
        Collection<Prototype> prototypes = fitPrototype.fit(encodedKanji.getImage());

        showRaster(encodedKanji, prototypes.iterator().next());

        Thread.sleep(Long.MAX_VALUE);
    }


    private void showRaster(EncodedKanji encodedKanji, Prototype prototype) throws InterruptedException {
        RasterVisualizer2.showRasterFlow(
                new RasterRun<ColorCell>() {
                    private int current = 0;

                    @Override
                    public boolean[][] getRawInput() {
                        return encodedKanji.getImage();
                    }

                    @Override
                    public boolean hasNext() {
                        return current < 1;
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
                        return new ColorCell(row, column, Color.BLUE, Collections.emptyList(), prototype.getSegments().get(0).getPairs());
                    }

                    @Override
                    public void next() {
                        ++current;
                    }
                },
                ImmutableList.of(new ColorPainter()));

    }


}
