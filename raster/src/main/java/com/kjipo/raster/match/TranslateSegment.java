package com.kjipo.raster.match;

import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentImpl;

import java.util.List;
import java.util.stream.Collectors;

public class TranslateSegment {

    public static Segment updateMatch(Segment segment, FlowDirection flowDirection, int numberOfRows, int numberOfColumns) {
        List<Pair> pairs = segment.getPairs().stream().map(pair -> new Pair(pair.getRow() + flowDirection.getRowShift(),
                pair.getColumn() + flowDirection.getColumnShift()))
                .collect(Collectors.toList());

        return new SegmentImpl(pairs);
    }


}
