package com.kjipo.raster.stochasticflow;

import visualization.RasterRun;

import java.util.List;

public final class RasterRunCreator {


    private RasterRunCreator() {

    }


    public static RasterRun<StochasticCell> createRasterRun(List<StochasticFlowRasterImpl> rasters, int rows, int columns, boolean rawRaster[][]) {
        StochasticRasterRun stochasticRasterRun = new StochasticRasterRun(rasters, rows, columns, rawRaster);

        return stochasticRasterRun;


    }


}
