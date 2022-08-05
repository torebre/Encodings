package com.kjipo.raster.attraction;


import com.kjipo.representation.prototype.Prototype;
import com.kjipo.representation.segment.Segment;

import java.util.List;

public class PrototypeImpl implements Prototype {
    private final List<Segment> segments;


    public PrototypeImpl(List<Segment> segments) {
        this.segments = segments;
    }


    @Override
    public List<Segment> getSegments() {
        return segments;
    }
}
