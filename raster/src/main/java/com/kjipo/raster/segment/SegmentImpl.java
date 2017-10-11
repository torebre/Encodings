package com.kjipo.raster.segment;

import java.util.List;

public class SegmentImpl implements Segment {
    private final List<Pair> pairs;


    public SegmentImpl(List<Pair> pairs) {
        if (pairs.isEmpty()) {
            throw new IllegalArgumentException("List of pairs cannot be empty");
        }
        this.pairs = pairs;
    }


    @Override
    public List<Pair> getPairs() {
        return pairs;
    }
}
