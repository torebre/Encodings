package com.kjipo.raster.attraction;

import com.kjipo.representation.segment.Segment;

public interface LineMoveOperation {

    Segment applyToLine(Segment linePrototype);
}
