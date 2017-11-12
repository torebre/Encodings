package com.kjipo.raster.attraction;


import com.kjipo.raster.segment.Segment;

public class ScaleOperation implements LineMoveOperation{
    private final int scaling;

    public ScaleOperation(int scaling) {
        this.scaling = scaling;
    }

    public int getScaling() {
        return scaling;
    }

    @Override
    public Segment applyToLine(Segment linePrototype) {
        // TODO
        return linePrototype;
    }
}
