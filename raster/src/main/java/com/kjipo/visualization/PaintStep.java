package com.kjipo.visualization;

import javafx.scene.shape.Rectangle;

public interface PaintStep {

    void process(RasterRun rasterRun, Rectangle raster[][]);

}
