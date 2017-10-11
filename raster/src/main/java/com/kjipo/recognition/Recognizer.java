package com.kjipo.recognition;


import com.kjipo.raster.Cell;
import com.kjipo.raster.segment.Segment;
import com.kjipo.segmentation.KanjiSegmenter;
import com.kjipo.segmentation.RasterTransformer;

import java.util.List;

public class Recognizer {


    public void recognize(boolean inputData[][]) {
        Cell[][] flowRaster = RasterTransformer.segmentTransformer(inputData);

        List<Segment> segments = KanjiSegmenter.segmentKanji(flowRaster);


        // TODO
        Segment inputSegmentData = segments.get(0);
//        List<List<Segment>> segmentLines = SegmentMatcher.positionPrototype(flowRaster.length, flowRaster[0].length,
//                inputSegmentData, testPrototype2);
//
//        List<Segment> joinedSegmentLines = RecognitionUtilities.joinSegmentLines(segmentLines);


    }


}
