package com.kjipo.recognition;

import com.kjipo.raster.match.UnionSegment;
import com.kjipo.raster.segment.Segment;

import java.util.ArrayList;
import java.util.List;

public final class RecognitionUtilities {

    private RecognitionUtilities() {

    }


    public static List<Segment> joinSegmentLines(List<List<Segment>> segmentLines) {
        List<Segment> result = new ArrayList<>();
        List<Segment> previousSegmentInLine = new ArrayList<>();

        segmentLines.forEach(segmentLine -> previousSegmentInLine.add(segmentLine.get(0)));

        boolean foundNewElement = true;
        int counter = 0;
        while (foundNewElement) {
            foundNewElement = false;

            List<Segment> segmentsInStep = new ArrayList<>();
            int lineCounter = 0;
            for (List<Segment> segmentLine : segmentLines) {
                if (counter < segmentLine.size()) {
                    foundNewElement = true;
                    segmentsInStep.add(segmentLine.get(counter));
                    previousSegmentInLine.set(lineCounter, segmentLine.get(counter));
                } else {
                    segmentsInStep.add(previousSegmentInLine.get(lineCounter));
                }

                ++lineCounter;
            }

            ++counter;
            result.add(new UnionSegment(segmentsInStep));
        }

        return result;
    }


}
