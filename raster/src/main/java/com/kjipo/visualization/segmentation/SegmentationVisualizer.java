package com.kjipo.visualization.segmentation;

import com.google.common.collect.ImmutableList;
import com.kjipo.representation.segment.Pair;
import com.kjipo.representation.segment.Segment;
import com.kjipo.representation.EncodedKanji;
import com.kjipo.visualization.RasterRun;
import com.kjipo.visualization.RasterVisualizer2;
import javafx.scene.paint.Color;

import java.util.List;

public class SegmentationVisualizer {


    public static void showRasterFlow(EncodedKanji encodedKanji,
                                      Color colorRaster[][],
                                      List<Pair> segmentData,
                                      List<Segment> joinedSegmentLines) throws InterruptedException {
        showRasterFlow(encodedKanji.getImage(), colorRaster, segmentData, joinedSegmentLines);
    }


    public static void showRasterFlow(boolean image[][],
                                      Color colorRaster[][],
                                      List<Pair> segmentData,
                                      List<Segment> joinedSegmentLines) throws InterruptedException {
        RasterVisualizer2.showRasterFlow(
                new RasterRun<ColorCell>() {
                    private int current = 0;

                    @Override
                    public boolean[][] getRawInput() {
                        return image;
                    }

                    @Override
                    public boolean hasNext() {
                        return current < joinedSegmentLines.size() - 1;
                    }

                    @Override
                    public int getColumns() {
                        return image[0].length;
                    }

                    @Override
                    public int getRows() {
                        return image.length;
                    }

                    @Override
                    public ColorCell getCell(int row, int column) {
                        return new ColorCell(row, column, colorRaster[row][column], segmentData, joinedSegmentLines.get(current).getPairs());
                    }

                    @Override
                    public void next() {
                        ++current;
                    }
                },
                ImmutableList.of(new ColorPainter()));

    }

}
