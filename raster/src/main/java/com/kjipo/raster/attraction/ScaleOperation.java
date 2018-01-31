package com.kjipo.raster.attraction;


import com.google.common.collect.Lists;
import com.kjipo.prototype.LinePrototype;
import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;

import java.util.List;

public class ScaleOperation implements LineMoveOperation {
    private final int scaling;

    public ScaleOperation(int scaling) {
        this.scaling = scaling;
    }

    @Override
    public Segment applyToLine(Segment linePrototype) {
        List<Pair> pairs = Lists.newArrayList(linePrototype.getPairs());

        // TODO This might not work properly if scaling is large
        if (scaling > 0) {
            Pair lastPair = pairs.get(pairs.size() - 1);
            Pair nextToLastPair = pairs.get(pairs.size() - 2);

            FlowDirection flowDirection = EncodingUtilities.computeDirection(nextToLastPair, lastPair);

            // A line prototype has only one segment
            return new LinePrototype(pairs.get(0), Pair.of(lastPair.getRow() + scaling * flowDirection.getRowShift(), lastPair.getColumn() + scaling * flowDirection.getColumnShift())).getSegments().get(0);

        } else if (scaling < 0) {
            return new LinePrototype(pairs.get(0), pairs.get(pairs.size() - scaling)).getSegments().get(0);
        }

        // No scaling
        return linePrototype;

    }

    public int getScaling() {
        return scaling;
    }

    @Override
    public String toString() {
        return "ScaleOperation{" +
                "scaling=" + scaling +
                '}';
    }
}
