package com.kjipo.raster.attraction;

import com.kjipo.prototype.Prototype;
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
    private static final int MAX_ITERATIONS = 20;
    private static final int SQUARE_SIDE = 20;

    private static final Logger LOG = LoggerFactory.getLogger(SegmentMatcher.class);


    /**
     * Returns a list of list with segments that show the
     * transformation each segment has gone through.
     */
    public static List<List<Segment>> matchSegments(int numberOfRows, int numberOfColumns,
                                                    List<Segment> originalSegmentData, Prototype prototype) {
        List<List<Segment>> segmentLines = new ArrayList<>();

        // TODO Support multiple segments
        Segment originalSegment = originalSegmentData.get(0);

        List<Segment> segments = matchSingleSegment(numberOfRows, numberOfColumns, originalSegment, prototype.getSegments().iterator().next().getPairs());

        MoveOperation moveOperation = rotateSegments(originalSegment, segments.get(segments.size() - 1));

        segmentLines.add(segments);

        for (int i = 1; i < originalSegmentData.size(); ++i) {
            Segment segment = applyMoveOperation(originalSegmentData.get(i), moveOperation, numberOfRows, numberOfColumns);
            segmentLines.add(Collections.singletonList(segment));
        }

        return segmentLines;
    }


    public static List<List<Segment>> positionPrototype(int numberOfRows, int numberOfColumns,
                                                        Segment originalSegmentData, Prototype prototype) {
        List<Segment> prototypeSegments = new ArrayList<>(prototype.getSegments());

        // Select a segment in the prototype
        Segment startSegment = prototypeSegments.get(0);
        return matchSegment(numberOfRows, numberOfColumns, originalSegmentData, startSegment, prototypeSegments);
    }

    public static List<List<Segment>> matchSegment(int numberOfRows, int numberOfColumns,
                                                   Segment originalSegmentData, Segment startSegment,
                                                   List<Segment> segmentsInPrototype) {
        // Move the segment in the prototype to the segment given as input
        List<MoveScore> moveOperations = matchSingleSegment(numberOfRows, numberOfColumns,
                originalSegmentData.getPairs(), startSegment.getPairs());

        return moveSegments(numberOfRows, numberOfColumns, moveOperations, segmentsInPrototype);
    }


    public static List<List<Segment>> moveSegments(int numberOfRows,
                                                   int numberOfColumns,
                                                   List<MoveScore> moveOperations,
                                                   List<Segment> segmentsInPrototype) {
        List<List<Segment>> segmentLines = new ArrayList<>();

        // Apply the move operation to the segments in the prototype
        for (Segment prototypeSegment : segmentsInPrototype) {
            List<Segment> segmentLine = new ArrayList<>(moveOperations.size() + 1);
            segmentLine.add(prototypeSegment);
            segmentLines.add(segmentLine);
        }

        // Go through the list of move operations, apply one by
        // one and store all intermediate and final result in
        // segmentLines
        for (int i = 0; i < moveOperations.size(); ++i) {
            for (List<Segment> segmentLine : segmentLines) {
                Segment segment = applyMoveOperation(segmentLine.get(segmentLine.size() - 1), moveOperations.get(i).getMoveOperation(),
                        numberOfRows, numberOfColumns);
                segmentLine.add(segment);
            }
        }

        return segmentLines;


    }


    public static void addRemainingSegments(Segment originalSegmentData, Prototype prototype,
                                            Segment prototypeMatch, Segment matchedTo) {
        for (Segment segment : prototype.getSegments()) {
            if (segment == prototypeMatch) {
                continue;
            }
            // TODO
            getAngle(segment, prototypeMatch);
        }


    }


    private static double getAngle(Segment segment1, Segment segment2) {
        // TODO

        return 0.0;
    }


    private static List<Segment> matchSingleSegment(int numberOfRows, int numberOfColumns,
                                                    Segment originalSegment, List<Pair> prototypeData) {
        List<Segment> segments = new ArrayList<>();

        segments.add(new SegmentWithOriginal(originalSegment.getPairs(), originalSegment.getPairs(), 0,
                SQUARE_SIDE, numberOfRows, numberOfColumns, new MoveOperation(0, 0, 0, 0, 0)));

        boolean prototypeRaster[][] = EncodingUtilities.computeRasterBasedOnPairs(numberOfRows, numberOfColumns, prototypeData);
        int[][] distanceMap = MatchDistance.computeDistanceMap(prototypeRaster);

        for (int i = 1; i < MAX_ITERATIONS; ++i) {
            Segment segment = segments.get(i - 1);

            Segment nextSegment =
                    RotateSegment.updateMatch(new SegmentWithOriginal(
                            segment.getPairs(),
                            segment.getPairs(),
                            0,
                            SQUARE_SIDE,
                            numberOfRows,
                            numberOfColumns,
                            new MoveOperation(0, 0, 0, 0, 0)));

            int minDistance = MatchDistance.computeDistanceBasedOnDistanceMap(
                    EncodingUtilities.computeRasterBasedOnPairs(numberOfRows, numberOfColumns, nextSegment.getPairs()),
                    distanceMap);

            LOG.info("Min distance: {}", minDistance);

            for (FlowDirection flowDirection : FlowDirection.values()) {
                Segment segment1 = TranslateSegment.updateMatch(segment.getPairs(), flowDirection);

                int distance = MatchDistance.computeDistanceBasedOnDistanceMap(
                        EncodingUtilities.computeRasterBasedOnPairs(numberOfRows, numberOfColumns, segment1.getPairs()),
                        distanceMap);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Flow direction: {}. Distance: {}", flowDirection, distance);
                }

                if (distance < minDistance) {
                    nextSegment = segment1;
                    minDistance = distance;
                }
            }

            segments.add(nextSegment);
        }

        return segments;
    }

    private static List<MoveScore> matchSingleSegment(int numberOfRows, int numberOfColumns,
                                                      List<Pair> stationaryPairs, List<Pair> movablePairs) {
        List<MoveScore> moveOperations = new ArrayList<>();
        boolean prototypeRaster[][] = EncodingUtilities.computeRasterBasedOnPairs(numberOfRows, numberOfColumns, stationaryPairs);
        int[][] distanceMap = MatchDistance.computeDistanceMap(prototypeRaster);
        List<Pair> previousCoordinates = movablePairs;

        for (int i = 0; i < MAX_ITERATIONS; ++i) {
            SegmentWithOriginal rotatedSegment = RotateSegment.rotateSegment45DegreesCounterClockwise(previousCoordinates, numberOfRows, numberOfColumns, SQUARE_SIDE);
            MoveOperation moveOperation = rotatedSegment.getMoveOperation();
            Segment currentSegment = rotatedSegment;

            int minDistance = MatchDistance.computeDistanceBasedOnDistanceMap(
                    EncodingUtilities.computeRasterBasedOnPairs(numberOfRows, numberOfColumns, currentSegment.getPairs()),
                    distanceMap);

            LOG.info("Min distance: {}", minDistance);

            for (FlowDirection flowDirection : FlowDirection.values()) {
                Segment segment1 = TranslateSegment.updateMatch(previousCoordinates, flowDirection);

                boolean validCoordinates = true;
                for (Pair pair : segment1.getPairs()) {
                    if (pair.getRow() < 0
                            || pair.getRow() >= numberOfRows
                            || pair.getColumn() < 0
                            || pair.getColumn() >= numberOfColumns) {
                        validCoordinates = false;
                        break;
                    }
                }

                if (!validCoordinates) {
                    continue;
                }

                MoveOperation translatedMovement = new MoveOperation(flowDirection.getRowShift(), flowDirection.getColumnShift(), 0, 0, 0);

                int distance = MatchDistance.computeDistanceBasedOnDistanceMap(
                        EncodingUtilities.computeRasterBasedOnPairs(numberOfRows, numberOfColumns, segment1.getPairs()),
                        distanceMap);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Flow direction: {}. Distance: {}", flowDirection, distance);
                }

                if (distance < minDistance) {
                    currentSegment = segment1;
                    moveOperation = translatedMovement;
                    minDistance = distance;
                }
            }


            previousCoordinates = currentSegment.getPairs();
            moveOperations.add(new MoveScore(moveOperation, minDistance));
        }

        return moveOperations;
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
        List<Pair> translatedCoordinates = segment.getPairs().stream()
                .map(pair -> Pair.of(pair.getRow() + moveOperation.getRowOffset(), pair.getColumn() + moveOperation.getColumnOffset()))
                .collect(Collectors.toList());
        List<Pair> rotatedCoordinates;

        if (Math.abs(moveOperation.getRotation()) > 0.001) {
            rotatedCoordinates = RotateSegment.rotateSegment(translatedCoordinates,
                    moveOperation.getPivotRow(),
                    moveOperation.getPivotColumn(),
                    numberOfRows,
                    numberOfColumns,
                    SQUARE_SIDE,
                    moveOperation.getRotation());
        } else {
            rotatedCoordinates = translatedCoordinates;
        }

        return new SegmentImpl(rotatedCoordinates);
    }


}
