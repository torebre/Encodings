package com.kjipo.raster.attraction;


import com.kjipo.raster.segment.Segment;

import java.util.Collection;

public class PrototypeImpl implements Prototype {
    private final Collection<Segment> segments;


    public PrototypeImpl(Collection<Segment> segments) {
        this.segments = segments;
    }


    @Override
    public Collection<Segment> getSegments() {
        return segments;
    }
}
