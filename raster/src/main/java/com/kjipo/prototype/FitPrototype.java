package com.kjipo.prototype;

import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.attraction.MoveOperation;
import com.kjipo.raster.attraction.SegmentMatcher;
import com.kjipo.raster.match.MatchDistance;
import com.kjipo.raster.match.RotateSegment;
import com.kjipo.raster.match.TranslateSegment;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentWithOriginal;
import com.kjipo.recognition.RecognitionUtilities;
import com.kjipo.segmentation.KanjiSegmenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.Line;
import java.util.*;
import java.util.stream.Collectors;

public class FitPrototype {

    private static final int MAX_ITERATIONS = 1000;

    private static final Logger LOG = LoggerFactory.getLogger(FitPrototype.class);


    public Collection<Prototype> fit(boolean inputData[][]) {
        int numberOfRows = inputData.length;
        int numberOfColumns = inputData[0].length;

        List<MoveOperation> moveOperations = new ArrayList<>();

        List<Prototype> result = new ArrayList<>();

        int[][] distanceMap = MatchDistance.computeDistanceMap(inputData);

        LinePrototype linePrototype = new LinePrototype(Pair.of(0, 0), Pair.of(0, 1));
        int minDistance = computeDistance(linePrototype.getSegments().get(0).getPairs(), distanceMap);

        for (int i = 0; i < MAX_ITERATIONS; ++i) {
            Map<LinePrototype, Integer> lineDistanceMap = new HashMap<>();

            // A line prototype only has one segment
            Segment segment = linePrototype.getSegments().iterator().next();

            kotlin.Pair<LinePrototype, Integer> newPrototype = linePrototype.getMovements()
                    .map(linePrototype1 -> {
                        Segment segment1 = linePrototype1.getSegments().get(0);
                        Pair startPair = segment1.getPairs().get(0);
                        Pair endPair = segment1.getPairs().get(segment1.getPairs().size() - 1);

                        if (!validCoordinates(startPair, numberOfRows, numberOfColumns)
                                || !validCoordinates(endPair, numberOfRows, numberOfColumns)) {
                            return null;
                        }

                        return new kotlin.Pair<>(new LinePrototype(startPair, endPair), computeDistance(segment1.getPairs(), distanceMap));
                    })
                    .filter(Objects::nonNull)
                    .reduce(new kotlin.Pair<>(new LinePrototype(new Pair(0, 0), new Pair(0, 0)), Integer.MAX_VALUE),
                            (first, second) -> {
                                if (first.getSecond().compareTo(second.getSecond()) > 0) {
                                    return first;
                                } else {
                                    return second;
                                }
                            });

            if (newPrototype.getSecond() >= minDistance) {
                break;
            }

            linePrototype = newPrototype.getFirst();
            minDistance = newPrototype.getSecond();
        }


        // TODO For now only returning one prototype
        result.add(linePrototype);


        return result;


    }


    private static int computeDistance(List<Pair> pairs, int distanceMatrix[][]) {
        return pairs.stream().mapToInt(pair -> distanceMatrix[pair.getRow()][pair.getColumn()]).sum();
    }

    private static boolean validCoordinates(Pair pair, int numberOfRows, int numberOfColumns) {
        return !(pair.getRow() < 0
                || pair.getRow() >= numberOfRows
                || pair.getColumn() < 0
                || pair.getColumn() >= numberOfColumns);
    }


}
