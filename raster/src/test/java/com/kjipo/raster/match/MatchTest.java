package com.kjipo.raster.match;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentImpl;
import com.kjipo.raster.segment.SegmentWithOriginal;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import visualization.CellType;
import visualization.RasterElementProcessor;
import visualization.RasterRun;
import visualization.RasterVisualizer2;

import java.util.ArrayList;
import java.util.List;

public class MatchTest {
    private final int numberOfRows = 20;
    private final int numberOfColumns = 20;


    private void matchLineSegments() throws InterruptedException {
        List<Pair> prototypeData = Lists.newArrayList(new Pair(15, 8),
                new Pair(16, 9),
                new Pair(17, 10));


        boolean inputData[][] = new boolean[numberOfRows][numberOfColumns];

        inputData[15][4] = true;
        inputData[15][5] = true;
        inputData[15][6] = true;

        List<Pair> originalSegmentData = new ArrayList<>();

        for(int row = 0; row < inputData.length; ++row) {
            for(int column = 0; column < inputData[0].length; ++column) {
                if(inputData[row][column]) {
                    originalSegmentData.add(new Pair(row, column));
                }
            }
        }

        List<SegmentWithOriginal> segments = new ArrayList<>();
        segments.add(new SegmentWithOriginal(originalSegmentData, originalSegmentData, 0,
                20, 20, 20));

        for (int i = 1; i < 10; ++i) {
            segments.add(MatchJoin.updateMatch(segments.get(i - 1)));
        }


        RasterVisualizer2.showRasterFlow(
                new RasterRun<MatchCell>() {
                    private int current = 0;

                    @Override
                    public boolean[][] getRawInput() {

                        return inputData;
                    }

                    @Override
                    public boolean hasNext() {
                        return current < 9;
                    }

                    @Override
                    public int getColumns() {
                        return numberOfColumns;
                    }

                    @Override
                    public int getRows() {
                        return numberOfRows;
                    }

                    @Override
                    public MatchCell getCell(int row, int column) {
                        return new MatchCell(row, column, segments.get(current).getPairs(), prototypeData);
                    }

                    @Override
                    public void next() {
                        ++current;
                    }
                },
                ImmutableList.of(new PrototypePainter()));


    }


    public class PrototypePainter implements RasterElementProcessor<MatchCell> {

        @Override
        public void processCell(MatchCell cell, int squareSize, ObservableList<Node> node, Rectangle rectangle) {
            if (cell.isSegmentData()) {
                rectangle.setFill(Color.GREEN);
            }
            if (cell.isPrototypeData()) {
                rectangle.setFill(Color.RED);
            }
        }
    }


    public class MatchCell implements CellType {
        private final int row;
        private final int column;

        private final List<Pair> segmentData;
        private final List<Pair> prototypeData;


        public MatchCell(int row, int column, List<Pair> segmentData, List<Pair> prototypeData) {
            this.row = row;
            this.column = column;
            this.segmentData = segmentData;
            this.prototypeData = prototypeData;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        public boolean isSegmentData() {
            return segmentData.contains(new Pair(row, column));
        }

        public boolean isPrototypeData() {
            return prototypeData.contains(new Pair(row, column));
        }
    }


    public static void main(String args[]) throws InterruptedException {
        MatchTest test = new MatchTest();
        test.matchLineSegments();

    }


}
