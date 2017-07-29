package com.kjipo.raster.attraction;

import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.match.MatchDistance;
import com.kjipo.raster.match.RotateSegment;
import com.kjipo.raster.match.TranslateSegment;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentImpl;
import com.kjipo.raster.segment.SegmentWithOriginal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SegmentMatcher {

    private static final double ROTATION_45_DEGREES = Math.PI / 4;

    private static final Logger LOG = LoggerFactory.getLogger(SegmentMatcher.class);


    public static List<List<Segment>> matchSegments(int numberOfRows, int numberOfColumns,
                                                    List<Segment> originalSegmentData, List<Pair> prototypeData) {
        List<List<Segment>> segmentLines = new ArrayList<>();

        // TODO Support multiple segments
        Segment originalSegment = originalSegmentData.get(0);

        List<Segment> segments = matchSingleSegment(numberOfRows, numberOfColumns, originalSegment, prototypeData);
        MoveOperation moveOperation = rotateSegments(originalSegment, segments.get(segments.size() - 1));

        segmentLines.add(segments);

        for (int i = 1; i < originalSegmentData.size(); ++i) {
            Segment segment = applyMoveOperation(originalSegmentData.get(i), moveOperation, numberOfRows, numberOfColumns);
            segmentLines.add(Collections.singletonList(segment));
        }

        return segmentLines;
    }

    private static List<Segment> matchSingleSegment(int numberOfRows, int numberOfColumns,
                                                    Segment originalSegment, List<Pair> prototypeData) {
        List<Segment> segments = new ArrayList<>();

        segments.add(new SegmentWithOriginal(originalSegment.getPairs(), originalSegment.getPairs(), 0,
                20, numberOfRows, numberOfColumns));

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

                if (distance < minDistance) {
                    nextSegment = segment1;
                    minDistance = distance;
                }
            }

            segments.add(nextSegment);
        }

        return segments;
    }


    private static MoveOperation rotateSegments(Segment originalPosition, Segment newPosition) {
        List<Pair> originalPositions = originalPosition.getPairs();
        Pair firstOriginal = originalPositions.get(0);
        Pair secondOriginal = originalPositions.get(1);

        List<Pair> newPositions = newPosition.getPairs();
        Pair firstNew = newPositions.get(0);
        Pair secondNew = newPositions.get(1);

        // First point determines translation
        int rowOffset = firstNew.getRow() - firstOriginal.getRow();
        int columnOffset = firstNew.getColumn() - firstOriginal.getColumn();

        // Second point determines rotation
        int firstRowOriginalOffset = firstOriginal.getRow() - secondOriginal.getRow();
        int firstColumnOriginalOffset = firstOriginal.getColumn() - secondOriginal.getColumn();

        int newRowOffset = firstNew.getRow() - secondNew.getRow();
        int newColumnOffset = firstNew.getColumn() - secondNew.getColumn();

        FlowDirection originalRotation = EncodingUtilities.determineOffset(firstRowOriginalOffset, firstColumnOriginalOffset);
        FlowDirection newRotation = EncodingUtilities.determineOffset(newRowOffset, newColumnOffset);

        double rotationAngle;
        if (originalRotation == null || newRotation == null) {
            LOG.error("No rotation angle");
            rotationAngle = 0;
        } else {
            int rotationSteps = originalRotation.ordinal() - newRotation.ordinal();
            rotationAngle = rotationSteps * ROTATION_45_DEGREES;
        }

        return new MoveOperation(rowOffset, columnOffset, rotationAngle, firstNew.getRow(), firstNew.getColumn());
    }


    private static Segment applyMoveOperation(Segment segment, MoveOperation moveOperation, int numberOfRows, int numberOfColumns) {
        List<Pair> translatedCoordinates = segment.getPairs().stream().map(pair -> Pair.of(pair.getRow() + moveOperation.getRowOffset(), pair.getColumn() + moveOperation.getColumnOffset())).collect(Collectors.toList());
        List<Pair> rotatedCoordinates;

        if (Math.abs(moveOperation.getRotation()) > 0.001) {
            rotatedCoordinates = RotateSegment.rotate(translatedCoordinates, moveOperation.getPivotRow(), moveOperation.getPivotColumn(),
                    numberOfRows, numberOfColumns, 20, moveOperation.getRotation());
        } else {
            rotatedCoordinates = translatedCoordinates;
        }

        return new SegmentImpl(rotatedCoordinates);
    }


}
