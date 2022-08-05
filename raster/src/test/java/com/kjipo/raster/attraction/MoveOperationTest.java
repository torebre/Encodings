package com.kjipo.raster.attraction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kjipo.prototype.FitPrototype;
import com.kjipo.representation.prototype.LinePrototype;
import com.kjipo.representation.prototype.Prototype;
import com.kjipo.representation.segment.Pair;
import com.kjipo.visualization.RasterRun;
import com.kjipo.visualization.RasterVisualizer2;
import com.kjipo.visualization.segmentation.ColorCell;
import com.kjipo.visualization.segmentation.ColorPainter;
import javafx.scene.paint.Color;
import org.junit.Test;

import java.util.Collections;
import java.util.List;


public class MoveOperationTest {

    @Test
    public void rotationTest() throws InterruptedException {
        boolean raster[][] = new boolean[20][20];
        LinePrototype right = new LinePrototype(new Pair(10, 10), new Pair(14, 10));

        MoveOperation moveOperation = new MoveOperation(0, 0, Math.PI / 4, 10, 10);

        List<Prototype> prototypes = FitPrototype.applyMoveOperations(right, Lists.newArrayList(moveOperation));

        rotationVisualization(raster, prototypes);

        Thread.sleep(Long.MAX_VALUE);

    }


    private void rotationVisualization(boolean raster[][], List<Prototype> prototypes) throws InterruptedException {
        RasterVisualizer2.showRasterFlow(
                new RasterRun<ColorCell>() {
                    private int current = -1;

                    @Override
                    public boolean[][] getRawInput() {
                        return raster;
                    }

                    @Override
                    public boolean hasNext() {
                        return current < prototypes.size() - 1;
                    }

                    @Override
                    public int getColumns() {
                        return raster[0].length;
                    }

                    @Override
                    public int getRows() {
                        return raster.length;
                    }

                    @Override
                    public ColorCell getCell(int row, int column) {
                        // A line only has one segment
                        List<Pair> pairs = prototypes.get(current).getSegments().get(0).getPairs();
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
