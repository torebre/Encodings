package com.kjipo.compare;

import com.google.common.collect.Lists;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.attraction.SegmentMatcher;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentImpl;
import com.kjipo.recognition.RecognitionUtilities;
import com.kjipo.visualization.segmentation.SegmentationVisualizer;
import javafx.scene.paint.Color;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CompareSegmentsTest {


    @Test
    public void relativePositionTest() {
        Segment segment = new SegmentImpl(Lists.newArrayList(Pair.of(0, 0), Pair.of(2, 2)));
        Segment segment2 = new SegmentImpl(Lists.newArrayList(Pair.of(1, 4), Pair.of(3, 4)));

        CompareSegments compareSegments = new CompareSegments();

        List<FlowDirection> flowDirections = compareSegments.determineRelativePosition(segment, segment2);

        flowDirections.forEach(System.out::println);


    }

    @Test
    public void comparingTest() throws InterruptedException {
        Segment segment = new SegmentImpl(Lists.newArrayList(Pair.of(0, 0), Pair.of(2, 2)));
        Segment segment2 = new SegmentImpl(Lists.newArrayList(Pair.of(1, 4), Pair.of(3, 4)));

        List<List<Segment>> segmentLines = SegmentMatcher.matchSegment(10, 10, segment2, segment, Collections.singletonList(segment));
        List<Segment> joinedSegmentLines = RecognitionUtilities.joinSegmentLines(segmentLines);

        boolean[][] raster = paintOnRaster(10, 10, Collections.singletonList(segment));

        Color colorRaster[][] = new Color[raster.length][raster[0].length];
        Color segmentColor = Color.RED.deriveColor(0, 0, 0, 0.1);

        for (Pair pair : segment.getPairs()) {
            colorRaster[pair.getRow()][pair.getColumn()] = segmentColor;
        }

        SegmentationVisualizer.showRasterFlow(raster, colorRaster, segment.getPairs(), joinedSegmentLines);

        Thread.sleep(Long.MAX_VALUE);

    }


    private static boolean[][] paintOnRaster(int numberOfRows, int numberOfColumns, Collection<Segment> segments) {
        boolean raster[][] = new boolean[numberOfRows][numberOfColumns];

        for (Segment segment : segments) {
            segment.getPairs().forEach(pair -> raster[pair.getRow()][pair.getColumn()] = true);
        }

        return raster;
    }


}