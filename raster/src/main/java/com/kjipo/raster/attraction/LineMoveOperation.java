package com.kjipo.raster.attraction;

import com.kjipo.raster.segment.Segment;

public interface LineMoveOperation {

    Segment applyToLine(Segment linePrototype);
}
