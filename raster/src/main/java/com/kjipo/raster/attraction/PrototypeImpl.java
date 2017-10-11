package com.kjipo.raster.attraction;


import com.kjipo.prototype.Prototype;
import com.kjipo.raster.segment.Segment;

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
