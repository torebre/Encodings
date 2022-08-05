package com.kjipo.raster.match;

import com.kjipo.representation.segment.Pair;
import com.kjipo.representation.segment.Segment;
import com.kjipo.representation.segment.SegmentImpl;
import com.kjipo.representation.raster.FlowDirection;

import java.util.List;
import java.util.stream.Collectors;

public class TranslateSegment {

    public static Segment updateMatch(List<Pair> coordinates, FlowDirection flowDirection) {
        List<Pair> pairs = coordinates.stream().map(pair -> new Pair(pair.getRow() + flowDirection.getRowShift(),
                pair.getColumn() + flowDirection.getColumnShift()))
                .collect(Collectors.toList());

        return new SegmentImpl(pairs);
    }


}
