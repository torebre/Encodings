package com.kjipo.raster.match;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentWithOriginal;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import visualization.CellType;
import visualization.RasterElementProcessor;
import visualization.RasterRun;
import visualization.RasterVisualizer2;

import java.util.ArrayList;
import java.util.List;

public class MatchTest {
    private final int numberOfRows = 20;
    private final int numberOfColumns = 20;

    private static final Logger LOG = LoggerFactory.getLogger(MatchTest.class);


    private void matchLineSegments() throws InterruptedException {
        List<Pair> prototypeData = Lists.newArrayList(new Pair(15, 8),
                new Pair(16, 9),
                new Pair(17, 10));


        boolean inputData[][] = new boolean[numberOfRows][numberOfColumns];

        inputData[3][4] = true;
        inputData[3][5] = true;
        inputData[3][6] = true;

        List<Pair> originalSegmentData = new ArrayList<>();

        for(int row = 0; row < inputData.length; ++row) {
            for(int column = 0; column < inputData[0].length; ++column) {
                if(inputData[row][column]) {
                    originalSegmentData.add(new Pair(row, column));
                }
            }
        }

        List<Segment> segments = new ArrayList<>();
        segments.add(new SegmentWithOriginal(originalSegmentData, originalSegmentData, 0,
                20, 20, 20));

        boolean prototypeRaster[][] = EncodingUtilities.computeRasterBasedOnPairs(numberOfRows, numberOfColumns, prototypeData);
        int[][] distanceMap = MatchDistance.computeDistanceMap(prototypeRaster);

        for (int i = 1; i < 20; ++i) {
            Segment segment = segments.get(i - 1);

            Segment nextSegment =
                    RotateSegment.updateMatch(new SegmentWithOriginal(
                            segment.getPairs(),
                            segment.getPairs(),
                            0,
                            20,
                            numberOfRows,
                            numberOfColumns));

            int minDistance = MatchDistance.computeDistanceBasedOnDistanceMap(
                    EncodingUtilities.computeRasterBasedOnPairs(numberOfRows, numberOfColumns, nextSegment.getPairs()),
                    distanceMap);

            LOG.info("Min distance: {}", minDistance);

            for (FlowDirection flowDirection : FlowDirection.values()) {
                Segment segment1 = TranslateSegment.updateMatch(segment, flowDirection, numberOfRows, numberOfColumns);

                int distance = MatchDistance.computeDistanceBasedOnDistanceMap(
                        EncodingUtilities.computeRasterBasedOnPairs(numberOfRows, numberOfColumns, segment1.getPairs()),
                        distanceMap);

                LOG.info("Flow direction: {}. Distance: {}", flowDirection, distance);

                if(distance < minDistance) {
                    nextSegment = segment1;
                    minDistance = distance;
                }
            }

            segments.add(nextSegment);
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
                        return current < segments.size() - 1;
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
