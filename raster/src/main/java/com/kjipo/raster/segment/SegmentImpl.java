package com.kjipo.raster.segment;

import java.util.List;

public class SegmentImpl implements Segment {
    private final List<Pair> pairs;


    public SegmentImpl(List<Pair> pairs) {
        this.pairs = pairs;
    }


    @Override
    public List<Pair> getPairs() {
        return pairs;
    }
}
