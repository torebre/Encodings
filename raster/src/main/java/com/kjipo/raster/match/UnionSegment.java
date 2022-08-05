package com.kjipo.raster.match;

import com.kjipo.representation.segment.Pair;
import com.kjipo.representation.segment.Segment;

import java.util.List;
import java.util.stream.Collectors;

public class UnionSegment implements Segment {
    private final List<Segment> segments;

    public UnionSegment(List<Segment> segments) {
        this.segments = segments;
    }

    @Override
    public List<Pair> getPairs() {
        return segments.stream().flatMap(segment -> segment.getPairs().stream()).collect(Collectors.toList());
    }
}
