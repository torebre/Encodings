package com.kjipo.raster.match;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kjipo.prototype.Prototype;
import com.kjipo.raster.attraction.PrototypeImpl;
import com.kjipo.raster.attraction.SegmentMatcher;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentImpl;
import com.kjipo.recognition.RecognitionUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kjipo.visualization.RasterRun;
import com.kjipo.visualization.RasterVisualizer2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MatchTest {
    private final int numberOfRows = 30;
    private final int numberOfColumns = 30;

    private static final Logger LOG = LoggerFactory.getLogger(MatchTest.class);


    private void matchPrototype() throws InterruptedException {
        boolean inputData[][] = new boolean[numberOfRows][numberOfColumns];

        Prototype prototype = getTestPrototype2();
        List<Segment> inputSegments = getTestData();
        addSegmentsToRaster(inputSegments, inputData);

        List<List<Segment>> segmentLines = SegmentMatcher.positionPrototype(numberOfRows, numberOfColumns,
                inputSegments.get(0), prototype);

        List<Segment> segments = RecognitionUtilities.joinSegmentLines(segmentLines);

        showRun(inputData, segments, inputSegments.get(0));

    }


    private void matchLineSegments() throws InterruptedException {
        boolean inputData[][] = new boolean[numberOfRows][numberOfColumns];

        Prototype prototype = getTestPrototype();
        List<Segment> inputSegments = getTestData();
        addSegmentsToRaster(inputSegments, inputData);

        List<List<Segment>> segmentLines = SegmentMatcher.matchSegments(numberOfRows, numberOfColumns, inputSegments, prototype);
        List<Segment> segments = RecognitionUtilities.joinSegmentLines(segmentLines);

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
                        return new MatchCell(row, column, segments.get(current).getPairs(), prototype.getSegments().iterator().next().getPairs());
                    }

                    @Override
                    public void next() {
                        ++current;
                    }
                },
                ImmutableList.of(new PrototypePainter()));


    }

    private void showRun(boolean inputData[][], List<Segment> prototypeMovements, Segment originalDataSegment) throws InterruptedException {
        RasterVisualizer2.showRasterFlow(
                new RasterRun<MatchCell>() {
                    private int current = 0;

                    @Override
                    public boolean[][] getRawInput() {
                        return inputData;
                    }

                    @Override
                    public boolean hasNext() {
                        return current < prototypeMovements.size() - 1;
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
                        return new MatchCell(row, column, originalDataSegment.getPairs(),
                                prototypeMovements.get(current).getPairs());
                    }

                    @Override
                    public void next() {
                        ++current;
                    }
                },
                ImmutableList.of(new PrototypePainter()));


    }


    private void addSegmentsToRaster(Collection<Segment> segments, boolean raster[][]) {
        segments.stream()
                .flatMap(segment -> segment.getPairs().stream())
                .forEach(segment -> raster[segment.getRow()][segment.getColumn()] = true);
    }


    private static Prototype getTestPrototype() {
        List<Pair> prototypeData = Lists.newArrayList(new Pair(15, 8),
                new Pair(16, 9),
                new Pair(17, 10));

        List<Segment> segments = new ArrayList<>();
        segments.add(new SegmentImpl(prototypeData));

        return new PrototypeImpl(segments);
    }

    public static Prototype getTestPrototype2() {
        List<Pair> prototypeData = Lists.newArrayList(new Pair(15, 8),
                new Pair(16, 9),
                new Pair(17, 10));

        List<Pair> prototypeData2 = Lists.newArrayList(new Pair(17, 9),
                new Pair(18, 9),
                new Pair(19, 9));


        List<Segment> segments = new ArrayList<>();
        segments.add(new SegmentImpl(prototypeData));
        segments.add(new SegmentImpl(prototypeData2));

        return new PrototypeImpl(segments);
    }


    private static List<Segment> getTestData() {
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

        return Lists.newArrayList(segment1, segment2, segment3, segment4, segment5);
    }


    public static void main(String args[]) throws InterruptedException {
        MatchTest test = new MatchTest();
//        test.matchLineSegments();
        test.matchPrototype();

    }


}
