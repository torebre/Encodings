package com.kjipo.raster.match;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kjipo.raster.attraction.SegmentMatcher;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentImpl;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kjipo.visualization.CellType;
import com.kjipo.visualization.RasterElementProcessor;
import com.kjipo.visualization.RasterRun;
import com.kjipo.visualization.RasterVisualizer2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MatchTest {
    private final int numberOfRows = 30;
    private final int numberOfColumns = 30;

    private static final Logger LOG = LoggerFactory.getLogger(MatchTest.class);


    private void matchLineSegments() throws InterruptedException {
        List<Pair> prototypeData = Lists.newArrayList(new Pair(15, 8),
                new Pair(16, 9),
                new Pair(17, 10));


        boolean inputData[][] = new boolean[numberOfRows][numberOfColumns];

        Segment segment1 = new SegmentImpl(ImmutableList.<Pair>builder()
                .add(Pair.of(3, 4))
                .add(Pair.of(3, 5))
                .add(Pair.of(3, 6))
                .build());

        Segment segment2 = new SegmentImpl(ImmutableList.<Pair>builder()
                .add(Pair.of(3, 3))
                .add(Pair.of(2, 3))
                .add(Pair.of(1, 3))
                .add(Pair.of(0, 3))
                .build());

        Segment segment3 = new SegmentImpl(ImmutableList.<Pair>builder()
                .add(Pair.of(0, 4))
                .add(Pair.of(0, 5))
                .add(Pair.of(0, 6))
                .build());

        Segment segment4 = new SegmentImpl(ImmutableList.<Pair>builder()
                .add(Pair.of(3, 7))
                .add(Pair.of(2, 7))
                .add(Pair.of(1, 7))
                .add(Pair.of(0, 7))
                .build());

        Segment segment5 = new SegmentImpl(ImmutableList.<Pair>builder()
                .add(Pair.of(5, 8))
                .add(Pair.of(6, 9))
                .add(Pair.of(7, 10))
                .add(Pair.of(8, 11))
                .build());

        List<Segment> inputSegments = Lists.newArrayList(segment1, segment2, segment3, segment4, segment5);

        addSegmentsToRaster(inputSegments, inputData);


        List<List<Segment>> segmentLines = SegmentMatcher.matchSegments(numberOfRows, numberOfColumns, inputSegments, prototypeData);

        List<Segment> segments = joinSegmentLines(segmentLines);


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

    private void addSegmentsToRaster(Collection<Segment> segments, boolean raster[][]) {
        segments.stream()
                .flatMap(segment -> segment.getPairs().stream())
                .forEach(segment -> raster[segment.getRow()][segment.getColumn()] = true);
    }

    private List<Segment> joinSegmentLines(List<List<Segment>> segmentLines) {
        List<Segment> result = new ArrayList<>();

        List<Segment> previousSegmentInLine = new ArrayList<>();

        segmentLines.forEach(segmentLine -> previousSegmentInLine.add(segmentLine.get(0)));

        boolean foundNewElement = true;
        int counter = 0;
        while(foundNewElement) {
            foundNewElement = false;

            List<Segment> segmentsInStep = new ArrayList<>();
            int lineCounter = 0;
            for (List<Segment> segmentLine : segmentLines) {
                if(counter < segmentLine.size()) {
                    foundNewElement = true;
                    segmentsInStep.add(segmentLine.get(counter));
                    previousSegmentInLine.set(lineCounter, segmentLine.get(counter));
                }
                else {
                    segmentsInStep.add(previousSegmentInLine.get(lineCounter));
                }

                ++lineCounter;
            }

            ++counter;
            result.add(new UnionSegment(segmentsInStep));
        }

        return result;
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
